#!/usr/bin/env python3
"""Deterministic localhost-only HTTP fixture for the Phase 0 Jolt probe."""
from http.server import BaseHTTPRequestHandler, HTTPServer
import json

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path != "/ok":
            self.send_error(404)
            return
        body = json.dumps({"ok": True, "message": "synthetic"}).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)
    def log_message(self, format, *args):
        pass

HTTPServer(("127.0.0.1", 18080), Handler).serve_forever()
