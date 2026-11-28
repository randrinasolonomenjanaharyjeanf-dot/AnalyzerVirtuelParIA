
import os
import json
import logging
import re

from fastapi import FastAPI, UploadFile, File, HTTPException
from google import genai
from google.genai import types

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("analyzer")

app = FastAPI(
    title="Analyzer Virtuel Par IA",
    version="1.0.0"
)

api_key = os.environ.get("GEMINI_API_KEY")

if not api_key:
    logger.error("GEMINI_API_KEY is missing from environment variables")

client = genai.Client(api_key=api_key) if api_key else None


@app.get("/")
def home():
    return {
        "status": "online",
        "server": "Analyzer Virtuel Par IA"
    }


@app.get("/health")
def health():
    return {"status": "healthy"}


@app.post("/analyze")
async def analyze(image: UploadFile = File(...)):

    if client is None:
        raise HTTPException(
            status_code=500,
            detail="GEMINI_API_KEY manquant sur Render"
        )

    allowed = ["image/png", "image/jpeg", "image/webp"]

    if image.content_type not in allowed:
        raise HTTPException(
            status_code=400,
            detail="Format image non supporté"
        )

    data = await image.read()

    if not data:
        raise HTTPException(
            status_code=400,
            detail="Image vide"
        )

    if len(data) > 10 * 1024 * 1024:
        raise HTTPException(
            status_code=413,
            detail="Image trop volumineuse"
        )

    prompt = """
Tu es un assistant d'analyse de captures de matchs
de football virtuel.

Examine attentivement cette image.

Extrais les noms des équipes V1 et V2, les cotes V1,
X et V2, ainsi que le tournoi si visible.

Ne devine jamais une cote manquante ou illisible.

Calcule les probabilités implicites normalisées
si les trois cotes sont valides.

Ces probabilités sont des estimations et ne garantissent
jamais le résultat réel d'un match virtuel.

Ne prétends jamais connaître le résultat à l'avance.

Réponds uniquement avec un objet JSON valide contenant :
team_v1, team_v2, tournament,
odds_v1, odds_x, odds_v2,
probability_v1, probability_x, probability_v2,
analysis, confidence, complete.

Utilise null pour les valeurs inconnues.
Les cotes et probabilités doivent être des nombres
ou null. confidence doit être un nombre ou null.
complete doit être true ou false.
"""

    try:
        logger.info("Début de l'analyse avec Gemini")

        response = client.models.generate_content(
            model="gemini-3.5-flash-lite",
            contents=[
                prompt,
                types.Part.from_bytes(
                    data=data,
                    mime_type=image.content_type
                )
            ],
            config=types.GenerateContentConfig(
                response_mime_type="application/json"
            )
        )

        output = response.text

        if not output:
            raise ValueError("Réponse vide de Gemini")

        output = output.strip()

        output = re.sub(r"^```(?:json)?\s*", "", output)
        output = re.sub(r"\s*```$", "", output)

        result = json.loads(output)

        logger.info("Analyse terminée avec succès")

        return {
            "success": True,
            "server": "Analyzer Virtuel Par IA",
            "result": result
        }

    except Exception as error:
        logger.exception("ERREUR PENDANT L'ANALYSE GEMINI")

        raise HTTPException(
            status_code=500,
            detail=f"{type(error).__name__}: {str(error)[:500]}"
        )
        
