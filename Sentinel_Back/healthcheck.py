import os
import sys
import urllib.request

port = os.environ.get("PORT", "8000")
try:
    urllib.request.urlopen(f"http://127.0.0.1:{port}/health", timeout=4)
except OSError:
    sys.exit(1)
