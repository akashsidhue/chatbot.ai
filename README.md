# AI Chatbot Setup Guide

This guide outlines the steps to set up an AI chatbot locally using Qdrant for vector storage and Ollama for running LLM models.

## Prerequisites
- Windows laptop
- Java installed
- Spring Boot for backend development
- Qdrant for vector database
- Ollama for running LLMs
- Python installed

---

## Step 1: Install and Run Qdrant

1. Download the Qdrant binary from the official website.
2. Extract the files and navigate to the directory.
3. Run Qdrant using the following command:
   ```
   qdrant.exe
   ```
4. Alternatively, run Qdrant using Docker:
   ```
   docker run -d --name qdrant \
   -p 6333:6333 \
   -v qdrant_storage:/qdrant/storage \
   qdrant/qdrant
   ```

---

## Step 2: Install and Run Ollama

1. Download and install Ollama from the official website.
2. Pull the required models:
   ```
   ollama pull llama3
   ollama pull mistral
   ```
3. Verify the installed models:
   ```
   ollama list
   ```
4. Run a model:
   ```
   ollama run llama3
   ```

---

## Step 3: Setting Up Qdrant Collections

1. Define a collection in Qdrant with the correct vector size.
2. Ensure that embeddings match the expected dimension (e.g., 768 for `all-mpnet-base-v2`).
3. Use Qdrant’s API to store issue-solution pairs.

---

## Step 4: Setting Up a Local Embedding Service

1. Install dependencies:
   ```
   pip install flask sentence-transformers
   ```
2. Create a Python script (`embedding_service.py`) and add the following code:
   ```python
   from flask import Flask, request, jsonify
   from sentence_transformers import SentenceTransformer
   
   app = Flask(__name__)
   model = SentenceTransformer("sentence-transformers/all-mpnet-base-v2")
   
   @app.route("/embed", methods=["POST"])
   def embed_text():
       try:
           data = request.json
           if "text" not in data:
               return jsonify({"error": "Missing 'text' field"}), 400
   
           text = data["text"]
           embeddings = model.encode(text).tolist()
   
           return jsonify({"embeddings": embeddings})
   
       except Exception as e:
           return jsonify({"error": str(e)}), 500
   
   if __name__ == "__main__":
       app.run(host="0.0.0.0", port=5000, debug=True)
   ```
3. Run the embedding service:
   ```
   python embedding_service.py
   ```

---

## Step 5: Running the Chatbot

1. Start the Spring Boot application.
2. Ensure the application connects to Qdrant and Ollama correctly.
3. Store predefined issue-resolution pairs in Qdrant.
4. Use the chatbot to query issues and retrieve or generate solutions.

---

## Step 6: Debugging and Verification

- Check Qdrant logs to ensure data is stored correctly.
- Verify embeddings are generated with the expected dimensions.
- Check which port Ollama is running on (default is `11434`).
- Test different queries and evaluate response accuracy.

---

This setup ensures a functional AI chatbot running locally on Windows with Qdrant as the vector database and Ollama for LLM inference.

