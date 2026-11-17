
import os
import json
import base64
from fastapi import FastAPI, UploadFile, File, HTTPException
from openai import OpenAI

app = FastAPI(
    title="Analyzer Virtuel Par IA",
    version="1.0.0"
)

client = OpenAI(
    api_key=os.environ.get("OPENAI_API_KEY")
)

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

    allowed = ["image/png", "image/jpeg", "image/webp"]

    if image.content_type not in allowed:
        raise HTTPException(
            status_code=400,
            detail="Format image non supporté"
        )

    data = await image.read()

    if len(data) > 10 * 1024 * 1024:
        raise HTTPException(
            status_code=413,
            detail="Image trop volumineuse"
        )

    encoded = base64.b64encode(data).decode("utf-8")

    mime = image.content_type
    image_url = f"data:{mime};base64,{encoded}"

    prompt = """
    Tu es un assistant d'analyse de captures de
    matchs de football virtuel.

    Examine attentivement cette image.

    Extrais :
    - Le nom de l'équipe V1
    - Le nom de l'équipe V2
    - Les cotes V1, X et V2
    - Le nom du tournoi si visible

    Vérifie que les cotes sont lisibles.
    Ne devine jamais une cote manquante.

    Calcule les probabilités implicites normalisées
    à partir des cotes décimales si les trois cotes
    sont valides.

    Ces probabilités ne sont pas des prédictions
    garanties du résultat réel.

    Réponds uniquement en JSON avec cette structure :

    {
      "team_v1": "",
      "team_v2": "",
      "tournament": "",
      "odds_v1": null,
      "odds_x": null,
      "odds_v2": null,
      "probability_v1": null,
      "probability_x": null,
      "probability_v2": null,
      "analysis": "",
      "confidence": "low",
      "complete": false
    }
    """

    try:
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

        result = json.loads(response.output_text)

        return {
            "success": True,
            "server": "Analyzer Virtuel Par IA",
            "result": result
        }

    except json.JSONDecodeError:
        raise HTTPException(
            status_code=502,
            detail="Réponse IA non valide"
        )

    except Exception as error:
        raise HTTPException(
            status_code=500,
            detail="Erreur du serveur IA"
      )
      
