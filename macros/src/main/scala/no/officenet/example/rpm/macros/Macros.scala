package no.officenet.example.rpm.macros

import language.experimental.macros
import scala.reflect.macros.Context

object Macros {
	def ?[T](expr: T): Option[T] = macro withNullGuards_impl[T]

	def withNullGuards_impl[T](c: Context)(expr: c.Expr[T]): c.Expr[Option[T]] = {
		import c.universe._

		def eqOp = newTermName("==").encodedName
		def nullTree = c.literalNull.tree
		def noneTree = reify(None).tree
		def optionApplyTree = Select(reify(Option).tree, newTermName("apply"))

		def wrapInOption(tree: Tree) = Apply(optionApplyTree, List(tree))

		def canBeNull(tree: Tree) = {
			val sym = tree.symbol
			val tpe = tree.tpe

			sym != null &&
				!sym.isModule && !sym.isModuleClass &&
				!sym.isPackage && !sym.isPackageClass &&
				!(tpe <:< typeOf[AnyVal])
		}

		def isInferredImplicitConversion(apply: Tree, fun: Tree, arg: Tree) =
			fun.symbol.isImplicit && (!apply.pos.isDefined || apply.pos == arg.pos)

		def nullGuarded(originalPrefix: Tree, prefixTree: Tree, whenNonNull: Tree => Tree): Tree =
			if (canBeNull(originalPrefix)) {
				val prefixVal = c.fresh()
				Block(
					ValDef(Modifiers(), prefixVal, TypeTree(null), prefixTree),
					If(
						Apply(Select(Ident(prefixVal), eqOp), List(nullTree)),
						noneTree,
						whenNonNull(Ident(prefixVal))
					)
				)
			} else whenNonNull(prefixTree)

		def addNullGuards(tree: Tree, whenNonNull: Tree => Tree): Tree = tree match {
			case Select(qualifier, name) =>
				addNullGuards(qualifier, guardedQualifier =>
					nullGuarded(qualifier, guardedQualifier, prefix => whenNonNull(Select(prefix, name))))
			case Apply(fun, List(arg)) if (isInferredImplicitConversion(tree, fun, arg)) =>
				addNullGuards(arg, guardedArg =>
					nullGuarded(arg, guardedArg, prefix => whenNonNull(Apply(fun, List(prefix)))))
			case Apply(Select(qualifier, name), args) =>
				addNullGuards(qualifier, guardedQualifier =>
					nullGuarded(qualifier, guardedQualifier, prefix => whenNonNull(Apply(Select(prefix, name), args))))
			case Apply(fun, args) =>
				addNullGuards(fun, guardedFun => whenNonNull(Apply(guardedFun, args)))
			case _ => whenNonNull(tree)
		}

		c.Expr[Option[T]](addNullGuards(expr.tree, tree => wrapInOption(tree)))
	}

	def any2question_impl[T, R >: T](c: Context {type PrefixType = any2question[T]})(default: c.Expr[R]): c.Expr[R] = {
		import c.universe._

		val Apply(_, List(prefix)) = c.prefix.tree
		val nullGuardedPrefix = withNullGuards_impl(c)(c.Expr[T](prefix))
		reify {
			nullGuardedPrefix.splice.getOrElse(default.splice)
		}
	}

	implicit class any2question[T](any: T) {
		def ?[R >: T](default: R): R = macro any2question_impl[T, R]
	}
}