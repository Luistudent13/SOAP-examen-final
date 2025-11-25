from spyne import Application, rpc, ServiceBase, Unicode
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication
import json
import psycopg2

# Config PostgreSQL (MISMO Railway que usa Spring Boot)
DB_HOST = "shinkansen.proxy.rlwy.net"
DB_PORT = 21388
DB_NAME = "railway"
DB_USER = "postgres"
DB_PASSWORD = "DlTFMilixrVWiZlueRGniZwcfFWWTtmy"

def get_connection():
    return psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        dbname=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )

class MatriculaService(ServiceBase):

    @rpc(Unicode, _returns=Unicode)
    def obtenerAlumnoPorMatricula(ctx, matricula):
        try:
            conn = get_connection()
            cur = conn.cursor()
            cur.execute(
                "SELECT matricula, nombre, programa FROM alumnos WHERE matricula = %s",
                (matricula,)
            )
            row = cur.fetchone()
            cur.close()
            conn.close()

            if row is None:
                return json.dumps({"error": "Alumno no encontrado"}, ensure_ascii=False)

            alumno = {
                "matricula": row[0],
                "nombre": row[1],
                "programa": row[2]
            }
            return json.dumps(alumno, ensure_ascii=False)

        except Exception as e:
            print("Error consultando PostgreSQL:", e)
            return json.dumps({"error": "Error interno en el servicio"}, ensure_ascii=False)

soap_app = Application(
    [MatriculaService],
    tns="http://uav.mx/matriculas",
    in_protocol=Soap11(validator="lxml"),
    out_protocol=Soap11()
)

wsgi_app = WsgiApplication(soap_app)

if __name__ == "__main__":
    from wsgiref.simple_server import make_server

    host = "0.0.0.0"
    port = 8000
    print(f"Servicio SOAP de Matrículas en http://{host}:{port}")
    print("WSDL: http://localhost:8000/?wsdl")  # ← aquí corrige el 8081, tu server está en 8000
    server = make_server(host, port, wsgi_app)
    server.serve_forever()
