package me.champeau.speakertime.support

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class ViewInjectionTransformation extends AbstractASTTransformation {
    private final static ClassNode VIEWBYID = ClassHelper.make(ViewById)

    @Override
    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        if (astNodes[1] instanceof ClassNode) {
            visitClass((ClassNode) astNodes[1])
        }
    }

    private void visitClass(ClassNode classNode) {
        def injectViews = classNode.getDeclaredMethod("injectViews")
        BlockStatement body = (BlockStatement) injectViews?.code
        if (!body) {
            body = new BlockStatement()
            classNode.addMethod("injectViews", ACC_PRIVATE, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body)
        }
        classNode.fields.each { field ->
            def ann = field.getAnnotations(VIEWBYID)
            ann.each {
                Expression idExpr = (Expression) it.getMember("value")
                def fvbid = new MethodCallExpression(new VariableExpression("this"), "findViewById", idExpr)
                fvbid.implicitThis = true
                fvbid.sourcePosition = field
                def assign = new BinaryExpression(
                        new VariableExpression(field),
                        Token.newSymbol(Types.EQUAL, -1, -1),
                        new CastExpression(
                                field.getOriginType(),
                                fvbid)
                )
                assign.sourcePosition = field
                ((BlockStatement) body).addStatement(new ExpressionStatement(assign))
            }
        }
    }
}