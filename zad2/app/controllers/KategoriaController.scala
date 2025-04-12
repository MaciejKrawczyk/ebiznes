package controllers

import javax.inject._
import play.api.mvc._
import scala.collection.mutable.ListBuffer
import play.api.libs.json._

@Singleton
class KategoriaController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // Definicja modelu Kategoria
  case class Kategoria(id: Long, nazwa: String)
  implicit val kategoriaFormat: OFormat[Kategoria] = Json.format[Kategoria]

  // Przykładowe dane kategorii
  private val kategorie = ListBuffer(
    Kategoria(1, "Elektronika"),
    Kategoria(2, "Moda"),
    Kategoria(3, "Dom i ogród")
  )

  // GET /kategorie - Lista wszystkich kategorii
  def listaKategorii() = Action { implicit request: Request[AnyContent] =>
    Ok(Json.toJson(kategorie))
  }

  // GET /kategorie/:id - Szczegóły kategorii o podanym id
  def kategoriaSzczegoly(id: Long) = Action { implicit request: Request[AnyContent] =>
    kategorie.find(_.id == id) match {
      case Some(kategoria) => Ok(Json.toJson(kategoria))
      case None => NotFound(Json.obj("error" -> s"Kategoria o id $id nie została znaleziona"))
    }
  }

  // POST /kategorie - Dodanie nowej kategorii
  def dodajKategorie() = Action(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Kategoria].fold(
      errors => BadRequest(Json.obj("error" -> "Nieprawidłowy format JSON")),
      kategoria => {
        kategorie += kategoria
        Created(Json.toJson(kategoria))
      }
    )
  }

  // PUT /kategorie/:id - Aktualizacja istniejącej kategorii
  def edytujKategorie(id: Long) = Action(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Kategoria].fold(
      errors => BadRequest(Json.obj("error" -> "Nieprawidłowy format JSON")),
      updatedKategoria => {
        kategorie.indexWhere(_.id == id) match {
          case -1 => NotFound(Json.obj("error" -> s"Kategoria o id $id nie istnieje"))
          case idx =>
            kategorie.update(idx, updatedKategoria.copy(id = id))
            Ok(Json.toJson(updatedKategoria.copy(id = id)))
        }
      }
    )
  }

  // DELETE /kategorie/:id - Usunięcie kategorii
  def usunKategorie(id: Long) = Action { implicit request: Request[AnyContent] =>
    kategorie.indexWhere(_.id == id) match {
      case -1 => NotFound(Json.obj("error" -> s"Kategoria o id $id nie istnieje"))
      case idx =>
        kategorie.remove(idx)
        Ok(Json.obj("message" -> s"Kategoria o id $id została usunięta"))
    }
  }
}
