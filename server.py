
import os
import json
import base64
import logging

from fastapi import FastAPI, UploadFile, File, HTTPException
from openai import OpenAI

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("analyzer")

app = FastAPI(
    title="Analyzer Virtuel Par IA",
    version="1.0.0"
)

api_key = os.environ.get("OPENAI_API_KEY")

if not api_key:
    logger.error("OPENAI_API_KEY is missing from environment variables")

client = OpenAI(api_key=api_key) if api_key else None


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
            detail="OPENAI_API_KEY manquant sur Render"
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

    encoded = base64.b64encode(data).decode("utf-8")
    image_url = f"data:{image.content_type};base64,{encoded}"

    prompt = """
Tu es un assistant d'analyse de captures de matchs
de football virtuel.

Examine attentivement cette image.

Extrais les noms des équipes V1 et V2, les cotes V1,
X et V2, ainsi que le tournoi si visible.

Ne devine jamais une cote manquante ou illisible.

Calcule les probabilités implicites normalisées
si les trois cotes sont valides.

Ces probabilités ne garantissent pas le résultat réel.

Réponds uniquement avec un objet JSON contenant :
team_v1, team_v2, tournament,
odds_v1, odds_x, odds_v2,
probability_v1, probability_x, probability_v2,
analysis, confidence, complete.

Utilise null pour les valeurs inconnues.
"""

    try:
        logger.info("Début de l'analyse IA")

        response = client.responses.create(
            model="gpt-5.5",
            input=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "input_text",
                            "text": prompt
                        },
                        {
                            "type": "input_image",
                            "image_url": image_url,
                            "detail": "high"
                        }
                    ]
                }
            ]
        )

        output = response.output_text

        if not output:
            raise ValueError("Réponse vide de l'IA")

        result = json.loads(output)

        logger.info("Analyse terminée avec succès")

        return {
            "success": True,
            "server": "Analyzer Virtuel Par IA",
            "result": result
        }

    except Exception as error:
        logger.exception("ERREUR PENDANT L'ANALYSE IA")

        raise HTTPException(
            status_code=500,
            detail=f"{type(error).__name__}: {str(error)[:500]}"
        )
        
