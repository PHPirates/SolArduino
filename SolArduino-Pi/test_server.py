from http.server import BaseHTTPRequestHandler, HTTPServer
import time

from test_panel_control import panels_stop, panels_down, panels_up

hostName = "192.168.8.42"
hostPort = 8080


class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(
            bytes("<html><head><title>SolArduino Pi</title></head>",
                  "utf-8"))
        if self.path == '/?panel=up':
            panels_up()
            self.wfile.write(bytes("<body><p>Panels going up.</p>", "utf-8"))
        elif self.path == '/?panel=down':
            panels_down()
            self.wfile.write(bytes("<body><p>Panels going down.</p>", "utf-8"))
        elif self.path == '/?panel=stop':
            panels_stop()
            self.wfile.write(bytes("<body><p>Panels stopping.</p>", "utf-8"))
        self.wfile.write(bytes("</body></html>", "utf-8"))


myServer = HTTPServer((hostName, hostPort), MyServer)
print(time.asctime(), "Server Starts - %s:%s" % (hostName, hostPort))

try:
    myServer.serve_forever()
except KeyboardInterrupt:
    pass

myServer.server_close()
print(time.asctime(), "Server Stops - %s:%s" % (hostName, hostPort))
