package com.kifi.macros

import scala.reflect.macros._
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation

import CrossVersionDefs._

object jsonMacroInstance extends jsonMacro(false)
object jsonStrictMacroInstance extends jsonMacro(true)

/**
 * "@json" macro annotation for case classes
 *
 * This macro annotation automatically creates a JSON serializer for the annotated case class.
 * The companion object will be automatically created if it does not already exist.
 *
 * If the case class has more than one field, the default Play formatter is used.
 * If the case class has only one field, the field is directly serialized. For example, if A
 * is defined as:
 *
 *     case class A(value: Int)
 *
 * then A(4) will be serialized as '4' instead of '{"value": 4}'.
 */
class json extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro jsonMacroInstance.impl
}

/**
 * "@jsonstrict" macro annotation for case classes
 *
 * Same as "@json" annotation, except that it always uses the default Play formatter.
 * For example, if A is defined as:
 *
 *     case class A(value: Int)
 *
 * then A(4) will be serialized as '{"value": 4}'.
 */
class jsonstrict extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro jsonStrictMacroInstance.impl
}

class jsonMacro(isStrict: Boolean) {
  def impl(c: CrossVersionContext)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def extractClassNameAndFields(classDecl: ClassDef) = {
      try {
        val q"case class $className(..$fields) extends ..$bases { ..$body }" = classDecl
        (className, fields)
      } catch {
        case _: MatchError => c.abort(c.enclosingPosition, "Annotation is only supported on case class")
      }
    }

    def jsonFormatter(className: TypeName, fields: List[ValDef]) = {
      fields.length match {
        case 0 => c.abort(c.enclosingPosition, "Cannot create json formatter for case class with no fields")
        case 1 if !isStrict => {
          // use the serializer for the field
          q"""
            implicit val jsonAnnotationFormat = {
              import play.api.libs.json._
              Format(
                __.read[${fields.head.tpt}].map(s => ${className.toTermName}(s)),
                new Writes[$className] { def writes(o: $className) = Json.toJson(o.${fields.head.name}) }
              )
            }
          """
        }
        case _ => {
          // use Play's macro
          q"implicit val jsonAnnotationFormat = play.api.libs.json.Json.format[$className]"
        }
      }
    }

    def modifiedCompanion(compDeclOpt: Option[ModuleDef], format: ValDef, className: TypeName) = {
      compDeclOpt map { compDecl =>
        // Add the formatter to the existing companion object
        val q"object $obj extends ..$bases { ..$body }" = compDecl
        q"""
          object $obj extends ..$bases {
            ..$body
            $format
          }
        """
      } getOrElse {
        // Create a companion object with the formatter
        q"object ${className.toTermName} { $format }"
      }
    }

    def modifiedDeclaration(classDecl: ClassDef, compDeclOpt: Option[ModuleDef] = None) = {
      val (className, fields) = extractClassNameAndFields(classDecl)
      val format = jsonFormatter(className, fields)
      val compDecl = modifiedCompanion(compDeclOpt, format, className)

      // Return both the class and companion object declarations
      c.Expr(q"""
        $classDecl
        $compDecl
      """)
    }

    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifiedDeclaration(classDecl)
      case (classDecl: ClassDef) :: (compDecl: ModuleDef) :: Nil => modifiedDeclaration(classDecl, Some(compDecl))
      case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    }
  }
}
