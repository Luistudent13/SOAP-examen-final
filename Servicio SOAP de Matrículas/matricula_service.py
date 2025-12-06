from spyne import Application, rpc, ServiceBase, Unicode
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication
import json
import mysql.connector
from mysql.connector import Error

DB_HOST = "metro.proxy.rlwy.net"   # Host público de Railway
DB_PORT = 52898                    # Puerto público
DB_NAME = "railway"
DB_USER = "root"
DB_PASSWORD = "zdmpTGvUGSJMMWMdfiyuBffvjHwkXawT"

def get_connection():
    return mysql.connector.connect(
        host=DB_HOST,
        port=DB_PORT,
        database=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD
    )


class MatriculaService(ServiceBase):
    """
    Servicio SOAP para gestionar alumnos de la UV.
    Todas las respuestas se envían como JSON dentro del sobre SOAP,
    para que el cliente REST (Spring) pueda parsearlas fácilmente.
    """

    # ========= 1) REGISTRAR ALUMNO =========
    @rpc(Unicode, Unicode, Unicode, _returns=Unicode)
    def registrarAlumno(ctx, matricula, nombre, programa):
        """
        Registra un nuevo alumno.
        - Si la matrícula ya existe, regresa un error.
        """
        try:
            conn = get_connection()
            cur = conn.cursor()

            # Verificar si ya existe la matrícula
            cur.execute(
                "SELECT 1 FROM alumnos WHERE matricula = %s",
                (matricula,),
            )
            if cur.fetchone() is not None:
                cur.close()
                conn.close()
                return json.dumps(
                    {"error": "La matrícula ya existe"},
                    ensure_ascii=False,
                )

            # Insertar nuevo alumno
            cur.execute(
                "INSERT INTO alumnos (matricula, nombre, programa) "
                "VALUES (%s, %s, %s)",
                (matricula, nombre, programa),
            )
            conn.commit()
            cur.close()
            conn.close()

            alumno = {
                "matricula": matricula,
                "nombre": nombre,
                "programa": programa,
            }
            return json.dumps(
                {
                    "mensaje": "Alumno registrado correctamente",
                    "alumno": alumno,
                },
                ensure_ascii=False,
            )
        except Exception as e:
            print("Error registrando alumno en PostgreSQL:", e)
            # Si hubo un error durante el INSERT, hacer rollback
            try:
                conn.rollback()
            except Exception:
                pass
            return json.dumps(
                {"error": "Error interno al registrar alumno"},
                ensure_ascii=False,
            )

    # ========= 2) EDITAR ALUMNO =========
    @rpc(Unicode, Unicode, Unicode, _returns=Unicode)
    def editarAlumno(ctx, matricula, nombre, programa):
        """
        Edita un alumno siguiendo el flujo:
        1. Consultar alumno por matrícula.
        2. Eliminar registro existente.
        3. Insertar registro con los nuevos datos.
        """
        try:
            conn = get_connection()
            cur = conn.cursor()

            # 1) Consultar alumno existente
            cur.execute(
                "SELECT matricula, nombre, programa "
                "FROM alumnos WHERE matricula = %s",
                (matricula,),
            )
            row = cur.fetchone()
            if row is None:
                cur.close()
                conn.close()
                return json.dumps(
                    {"error": "No existe un alumno con esa matrícula"},
                    ensure_ascii=False,
                )

            # 2) Eliminar alumno actual
            cur.execute(
                "DELETE FROM alumnos WHERE matricula = %s",
                (matricula,),
            )

            # 3) Insertar nuevo registro (misma matrícula, nuevos datos)
            cur.execute(
                "INSERT INTO alumnos (matricula, nombre, programa) "
                "VALUES (%s, %s, %s)",
                (matricula, nombre, programa),
            )

            conn.commit()
            cur.close()
            conn.close()

            alumno_editado = {
                "matricula": matricula,
                "nombre": nombre,
                "programa": programa,
            }
            return json.dumps(
                {
                    "mensaje": "Alumno editado correctamente",
                    "alumno": alumno_editado,
                },
                ensure_ascii=False,
            )
        except Exception as e:
            print("Error editando alumno en PostgreSQL:", e)
            try:
                conn.rollback()
            except Exception:
                pass
            return json.dumps(
                {"error": "Error interno al editar alumno"},
                ensure_ascii=False,
            )

    # ========= 3) ELIMINAR ALUMNO (OPCIONAL PERO ÚTIL) =========
    @rpc(Unicode, _returns=Unicode)
    def eliminarAlumno(ctx, matricula):
        """
        Elimina un alumno por matrícula.
        """
        try:
            conn = get_connection()
            cur = conn.cursor()

            # Verificar que exista
            cur.execute(
                "SELECT 1 FROM alumnos WHERE matricula = %s",
                (matricula,),
            )
            if cur.fetchone() is None:
                cur.close()
                conn.close()
                return json.dumps(
                    {"error": "No existe un alumno con esa matrícula"},
                    ensure_ascii=False,
                )

            # Eliminar
            cur.execute(
                "DELETE FROM alumnos WHERE matricula = %s",
                (matricula,),
            )
            conn.commit()
            cur.close()
            conn.close()

            return json.dumps(
                {"mensaje": "Alumno eliminado correctamente"},
                ensure_ascii=False,
            )
        except Exception as e:
            print("Error eliminando alumno en PostgreSQL:", e)
            try:
                conn.rollback()
            except Exception:
                pass
            return json.dumps(
                {"error": "Error interno al eliminar alumno"},
                ensure_ascii=False,
            )

    # ========= 4) CONSULTAR ALUMNO (YA LO TENÍAS) =========
    @rpc(Unicode, _returns=Unicode)
    def obtenerAlumnoPorMatricula(ctx, matricula):
        try:
            conn = get_connection()
            cur = conn.cursor()
            cur.execute(
                "SELECT matricula, nombre, programa "
                "FROM alumnos WHERE matricula = %s",
                (matricula,),
            )
            row = cur.fetchone()
            cur.close()
            conn.close()

            if row is None:
                return json.dumps(
                    {"error": "Alumno no encontrado"},
                    ensure_ascii=False,
                )

            alumno = {
                "matricula": row[0],
                "nombre": row[1],
                "programa": row[2],
            }
            return json.dumps(alumno, ensure_ascii=False)
        except Exception as e:
            print("Error consultando PostgreSQL:", e)
            return json.dumps(
                {"error": "Error interno en el servicio"},
                ensure_ascii=False,
            )


soap_app = Application(
    [MatriculaService],
    tns="http://uav.mx/matriculas",
    in_protocol=Soap11(validator="lxml"),
    out_protocol=Soap11(),
)

wsgi_app = WsgiApplication(soap_app)

if __name__ == "__main__":
    from wsgiref.simple_server import make_server

    host = "0.0.0.0"
    port = 8000
    print(f"Servicio SOAP de Matrículas en http://{host}:{port}")
    print("WSDL: http://localhost:8000/?wsdl")
    server = make_server(host, port, wsgi_app)
    server.serve_forever()
