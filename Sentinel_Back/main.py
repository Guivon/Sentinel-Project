import logging
import os

from flask import Flask, Response, jsonify

from config import validate_environment


def create_app() -> Flask:

    logging.basicConfig(
        level=os.environ.get("LOG_LEVEL", "INFO").upper(),
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )
    validate_environment()

    from analysis import analysis_bp
    from auth import auth_bp
    from community import community_bp

    app = Flask(__name__)
    app.config["MAX_CONTENT_LENGTH"] = 6 * 1024 * 1024

    @app.errorhandler(404)
    def not_found(_error: Exception) -> tuple[Response, int]:
        return jsonify({"erro": "Rota não encontrada"}), 404

    @app.errorhandler(405)
    def method_not_allowed(_error: Exception) -> tuple[Response, int]:
        return jsonify({"erro": "Método não permitido"}), 405

    @app.errorhandler(413)
    def file_too_large(_error: Exception) -> tuple[Response, int]:
        return jsonify({"erro": "Arquivo muito grande"}), 413

    @app.errorhandler(500)
    def internal_error(_error: Exception) -> tuple[Response, int]:
        return jsonify({"erro": "Erro interno. Tente novamente."}), 500

    app.register_blueprint(auth_bp)
    app.register_blueprint(analysis_bp)
    app.register_blueprint(community_bp)

    @app.route("/health")
    def health() -> tuple[Response, int]:
        return jsonify({"situacao": "ok", "versao": os.environ.get("APP_VERSAO", "dev")}), 200

    return app


app = create_app()


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
