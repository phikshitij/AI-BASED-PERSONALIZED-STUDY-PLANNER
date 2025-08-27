from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import google.generativeai as genai
from pydantic import BaseModel
import json

# ✅ Initialize FastAPI app
app = FastAPI()

# ✅ Enable CORS to allow frontend requests
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow requests from any origin
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ✅ Configure Gemini API Key
genai.configure(api_key="AIzaSyAB37LoW88Uy4XKa85WQHDNbuPUi1hKHr4")

# ✅ Define Request Model
class StudyPlanRequest(BaseModel):
    domain: str
    academic_level: str
    year: str
    subject: str
    module: str
    hours_per_day: int
    days: int
    learning_style: str

# ✅ JSON Validator - Ensures AI response is valid JSON
def validate_json_response(response_text):
    try:
        print("🔍 Raw AI Response:", response_text)  # ✅ Debugging Log
        
        # ✅ Clean response and remove code block formatting if any
        cleaned_text = response_text.strip().replace("```json", "").replace("```", "").strip()
        
        # ✅ Convert response to JSON
        study_plan = json.loads(cleaned_text)

        # ✅ Check if AI response is a list
        if not isinstance(study_plan, list):
            return {"error": "AI response is not a JSON array"}

        # ✅ Required keys in response
        required_keys = {"day", "topic", "hours", "key_points", "recommended_resources"}
        
        # ✅ Validate each day's study plan
        for day in study_plan:
            if not required_keys.issubset(set(day.keys())):
                return {"error": f"Missing keys in AI response: {set(day.keys()) - required_keys}"}

            # ✅ Validate resources format
            for resource in day["recommended_resources"]:
                if "type" not in resource or "title" not in resource or "link" not in resource:
                    return {"error": f"Invalid resource format: {resource}"}

        return study_plan  
    except json.JSONDecodeError:
        return {"error": "AI returned invalid JSON format", "debug_raw_response": response_text}

# ✅ Study Plan Generation API
@app.post("/generate-study-plan")
def generate_study_plan(request: StudyPlanRequest):
    try:
        # ✅ Load Gemini Model
        model = genai.GenerativeModel("gemini-1.5-pro-latest")

        # ✅ Refined Prompt to Ensure AI Returns Valid JSON
        prompt = (
            f"Generate a {request.days}-day structured study plan for '{request.module}' under '{request.subject}', "
            f"for a '{request.academic_level}' student in '{request.domain}' (Year {request.year}).\n"
            f"Each day should have {request.hours_per_day} hours of study, difficulty level using '{request.learning_style}' learning style.\n"
            f"Ensure the response is in **strict JSON format** without any explanations. Do NOT include extra text, only JSON.\n"
            f"Each day must include **exactly 3 recommended resources** from these sources:\n"
            f"1. Mumbai University Official Resources\n"
            f"2. Shaalaa.com\n"
            f"3. Studocu\n"
            f"4. YouTube\n"
            f"Format the response as strictly JSON:\n"
            f'''```json
            [
                {{
                    "day": "Day 1",
                    "topic": "Introduction to Topic",
                    "hours": {request.hours_per_day},
                    "key_points": ["Point 1", "Point 2", "Point 3"],
                    "recommended_resources": [
                        {{"type": "video", "title": "YouTube Video", "link": "https://youtube.com/..."}},
                        {{"type": "article", "title": "Shaalaa Notes", "link": "https://shaalaa.com/..."}},
                        {{"type": "pdf", "title": "Studocu PDF", "link": "https://studocu.com/..."}}
                    ]
                }}
            ]
            ```'''
        )
        
        # ✅ Get AI Response
        response = model.generate_content(prompt)

        # ✅ Validate AI Response
        validated_data = validate_json_response(response.text)
        
        # ✅ Return Processed Data or Error
        if "error" in validated_data:
            return validated_data  
        
        return validated_data  
    except Exception as e:
        return {"error": str(e)}

