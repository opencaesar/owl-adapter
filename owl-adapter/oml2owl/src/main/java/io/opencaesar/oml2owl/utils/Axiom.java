package io.opencaesar.oml2owl.utils;

import java.util.Set;

public abstract class Axiom {

    protected abstract class ClassExpressionSetAxiom extends Axiom {

        private Set<ClassExpression> set;

        protected ClassExpressionSetAxiom(Set<ClassExpression> set) {
            super();
            this.set = set;
        }

        protected Set<ClassExpression> getSet() {
            return set;
        }

        protected class DisjointClassesAxiom extends ClassExpressionSetAxiom {

            protected DisjointClassesAxiom(Set<ClassExpression> set) {
                super(set);
            }
        }

        protected class EquivalentClassesAxiom extends ClassExpressionSetAxiom {

            protected EquivalentClassesAxiom(Set<ClassExpression> set) {
                super(set);
            }
        }

        protected class DisjointUnionAxiom extends ClassExpressionSetAxiom {

            protected ClassExpression.Singleton c;

            protected DisjointUnionAxiom(ClassExpression.Singleton c, Set<ClassExpression> set) {
                super(set);
                this.c = c;
            }

            protected ClassExpression.Singleton getC() {
                return c;
            }
        }
    }

}
