from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer

# Initialize Flask app
app = Flask(__name__)

# Load the embedding model
model = SentenceTransformer("sentence-transformers/all-mpnet-base-v2")

@app.route("/embed", methods=["POST"])
def embed_text():
    try:
        data = request.json
        if "text" not in data:
            return jsonify({"error": "Missing 'text' field"}), 400

        text = data["text"]
        embeddings = model.encode(text).tolist()  # Convert NumPy array to list

        return jsonify({"embeddings": embeddings})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
