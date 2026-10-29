FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
# A hospedagem define $PORT; 2 workers bastam para começar
CMD gunicorn main:app --bind 0.0.0.0:${PORT:-8000} --workers 2 --timeout 60
