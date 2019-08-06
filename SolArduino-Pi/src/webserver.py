from http.server import BaseHTTPRequestHandler


class Webserver(BaseHTTPRequestHandler):
    # noinspection PyPep8Naming
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write(
            bytes('<html><head><title>SolArduino Pi</title></head><body>',
                  'utf-8'))
        if self.path == '/?panel=up':
            self.wfile.write(bytes('Panels going up.', 'utf-8'))
        elif self.path == '/?panel=down':
            self.wfile.write(bytes('Panels going down.', 'utf-8'))
        elif self.path == '/?panel=stop':
            self.wfile.write(bytes('Panels stopping.', 'utf-8'))
        elif self.path == '/?update':
            self.wfile.write(bytes('0 manual', 'utf-8'))
        elif self.path == '/?panel=auto':
            self.wfile.write(bytes('Not implemented yet', 'utf-8'))
        elif self.path.contains('/?degrees='):
            self.wfile.write(bytes('Not implemented yet', 'utf-8'))
        else:
            self.wfile.write(bytes('Available urls: todo', 'utf-8'))  # todo
        self.wfile.write(bytes('</body></html>', 'utf-8'))
