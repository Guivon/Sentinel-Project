from flask import Flask, jsonify
from auth import auth_bp
from analise import analise_bp

app = Flask(__name__)

app.register_blueprint(auth_bp)
app.register_blueprint(analise_bp)


@app.route("/health")
def health():
    # Usado pela hospedagem para saber se o servidor está vivo
    return jsonify({"status": "ok"}), 200


if __name__ == "__main__":
    # Só para teste local. Em produção roda via gunicorn (ver Dockerfile).
    app.run(host="0.0.0.0", port=5000, debug=True)
