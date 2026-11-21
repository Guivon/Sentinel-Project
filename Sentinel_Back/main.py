from flask import Flask, jsonify
from auth import auth_bp
from analise import analise_bp

app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = 6 * 1024 * 1024  # limite total do upload (6 MB)


@app.errorhandler(413)
def arquivo_grande(_e):
    return jsonify({"erro": "Arquivo muito grande"}), 413


app.register_blueprint(auth_bp)
app.register_blueprint(analise_bp)


@app.route("/health")
def health():
    return jsonify({"status": "ok"}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

@app.route("/health")
def health():
    return jsonify({"status": "ok", "versao": "2026-11-21-bedrock"}), 200
