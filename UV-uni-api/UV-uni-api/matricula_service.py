from spyne import Application, rpc, ServiceBase, Unicode
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication

import json
import mysql.connector
from mysql.connector import Error


# =========================
# Configuración de la BD
# =========================
DB_HOST = ""
DB_PORT =          # por ejemplo: 52898
DB_NAME = ""
DB_USER = ""
DB_PASSWORD = ""


def get_connection():
    """Crea y regresa una conexión nueva a MySQL."""
    return mysql.connector.connect(
        host=DB_HOST,
        port=DB_PORT,
        database=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
    )


# =========================
# Helpers de respuesta JSON
# =========================
def _json_ok(payload: dict) -> Unicode:
    """Convierte un dict a JSON UTF-8 sin escapar acentos."""
    return json.dumps(payload, ensure_ascii=False)


def _json_error(message: str) -> Unicode:
    """Crea una respuesta de error estándar."""
    return _json_ok({"error": message})


# =========================
# Servicio SOAP
# =========================
class MatriculaService(ServiceBase):
    """
    Servicio SOAP para gestionar alumnos de la UV.
    Las respuestas se envían como JSON dentro del sobre SOAP
    para que el cliente REST pueda parsearlas con facilidad.
    """

    @rpc(Unicode, Unicode, Unicode, _returns=Unicode)
    def registrarAlumno(ctx, matricula, nombre, programa):
        """
        Registra un nuevo alumno.
        Si la matrícula ya existe, devuelve un error.
        """
        conn = None
        cur = None

        try:
            conn = get_connection()
            cur = conn.cursor()

            # Verificar si ya existe
            cur.execute(
                "SELECT 1 FROM alumnos WHERE matricula = %s",
                (matricula,),
            )
            if cur.fetchone() is not None:
                return _json_error("La matrícula ya existe")

            # Insertar registro
            cur.execute(
                """
                INSERT INTO alumnos (matricula, nombre, programa)
                VALUES (%s, %s, %s)
                """,
                (matricula, nombre, programa),
            )
            conn.commit()

            alumno = {
                "matricula": matricula,
                "nombre": nombre,
                "programa": programa,
            }

            return _json_ok(
                {
                    "mensaje": "Alumno registrado correctamente",
                    "alumno": alumno,
                }
            )

        except Exception as e:
            print("Error registrando alumno en PostgreSQL:", e)
            if conn is not None:
                try:
                    conn.rollback()
                except Exception:
                    pass
            return _json_error("Error interno al registrar alumno")

        finally:
            if cur is not None:
                cur.close()
            if conn is not None:
                conn.close()

    @rpc(Unicode, Unicode, Unicode, _returns=Unicode)
    def editarAlumno(ctx, matricula, nombre, programa):
        """
        Edita un alumno siguiendo el flujo:
        1. Consultar alumno por matrícula.
        2. Eliminar registro existente.
        3. Insertar registro con los nuevos datos.
        """
        conn = None
        cur = None

        try:
            conn = get_connection()
            cur = conn.cursor()

            # Comprobar existencia
            cur.execute(
                """
                SELECT matricula, nombre, programa
                FROM alumnos
                WHERE matricula = %s
                """,
                (matricula,),
            )
            row = cur.fetchone()
            if row is None:
                return _json_error("No existe un alumno con esa matrícula")

            # Borrar registro previo
            cur.execute(
                "DELETE FROM alumnos WHERE matricula = %s",
                (matricula,),
            )

            # Insertar nuevo registro
            cur.execute(
                """
                INSERT INTO alumnos (matricula, nombre, programa)
                VALUES (%s, %s, %s)
                """,
                (matricula, nombre, programa),
            )

            conn.commit()

            alumno_editado = {
                "matricula": matricula,
                "nombre": nombre,
                "programa": programa,
            }

            return _json_ok(
                {
                    "mensaje": "Alumno editado correctamente",
                    "alumno": alumno_editado,
                }
            )

        except Exception as e:
            print("Error editando alumno en PostgreSQL:", e)
            if conn is not None:
                try:
                    conn.rollback()
                except Exception:
                    pass
            return _json_error("Error interno al editar alumno")

        finally:
            if cur is not None:
                cur.close()
            if conn is not None:
                conn.close()

    @rpc(Unicode, _returns=Unicode)
    def eliminarAlumno(ctx, matricula):
        """
        Elimina un alumno por matrícula.
        """
        conn = None
        cur = None

        try:
            conn = get_connection()
            cur = conn.cursor()

            # Verificar existencia
            cur.execute(
                "SELECT 1 FROM alumnos WHERE matricula = %s",
                (matricula,),
            )
            if cur.fetchone() is None:
                return _json_error("No existe un alumno con esa matrícula")

            # Eliminar
            cur.execute(
                "DELETE FROM alumnos WHERE matricula = %s",
                (matricula,),
            )
            conn.commit()

            return _json_ok({"mensaje": "Alumno eliminado correctamente"})

        except Exception as e:
            print("Error eliminando alumno en PostgreSQL:", e)
            if conn is not None:
                try:
                    conn.rollback()
                except Exception:
                    pass
            return _json_error("Error interno al eliminar alumno")

        finally:
            if cur is not None:
                cur.close()
            if conn is not None:
                conn.close()

    @rpc(Unicode, _returns=Unicode)
    def obtenerAlumnoPorMatricula(ctx, matricula):
        """
        Devuelve los datos de un alumno por matrícula.
        """
        conn = None
        cur = None

        try:
            conn = get_connection()
            cur = conn.cursor()

            cur.execute(
                """
                SELECT matricula, nombre, programa
                FROM alumnos
                WHERE matricula = %s
                """,
                (matricula,),
            )
            row = cur.fetchone()

            if row is None:
                return _json_error("Alumno no encontrado")

            alumno = {
                "matricula": row[0],
                "nombre": row[1],
                "programa": row[2],
            }
            return _json_ok(alumno)

        except Exception as e:
            print("Error consultando PostgreSQL:", e)
            return _json_error("Error interno en el servicio")

        finally:
            if cur is not None:
                cur.close()
            if conn is not None:
                conn.close()


# =========================
# Configuración SOAP / WSGI
# =========================
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
