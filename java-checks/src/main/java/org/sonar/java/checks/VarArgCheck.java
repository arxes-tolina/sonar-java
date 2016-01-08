/*
 * SonarQube Java
 * Copyright (C) 2012-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.java.resolve.JavaSymbol;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.NoSqale;

import java.util.Collections;
import java.util.List;

@Rule(
  // (Godin): https://jira.sonarsource.com/browse/RSPEC-923 ?
  key = "_placeholder_",
  name = "_placeholder_",
  priority = Priority.INFO,
  tags = {},
  status = "BETA")
@NoSqale
public class VarArgCheck extends SubscriptionBaseVisitor {

  @Override
  public List<Tree.Kind> nodesToVisit() {
    return Collections.singletonList(Tree.Kind.METHOD);
  }

  // (Godin): "mvn install" must be executed before first compilation in IntelliJ IDEA
  @Override
  public void visitNode(Tree tree) {
    // (Godin): I have an idea of more sophisticated algorithm, but this one might be good enough as a starter:
    if (((JavaSymbol.MethodJavaSymbol) ((MethodTree) tree).symbol()).isVarArgs()) {
      // (Godin): was hard to write previous line of code, because I didn't found a way to print a syntax tree.
      reportIssue(tree, "vararg");
    }
  }

}
