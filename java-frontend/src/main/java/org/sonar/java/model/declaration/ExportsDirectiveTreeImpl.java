/*
 * SonarQube Java
 * Copyright (C) 2012-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.java.model.declaration;

import com.google.common.collect.ImmutableList;

import org.sonar.java.ast.parser.ListTreeImpl;
import org.sonar.java.model.InternalSyntaxToken;
import org.sonar.plugins.java.api.tree.ExportsDirectiveTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.ListTree;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TreeVisitor;
import org.sonar.plugins.java.api.tree.TypeTree;

import javax.annotation.Nullable;

public class ExportsDirectiveTreeImpl extends ModuleDirectiveTreeImpl implements ExportsDirectiveTree {

  private final ExpressionTree packageName;
  @Nullable
  private final InternalSyntaxToken toKeyword;
  private final ListTreeImpl<TypeTree> moduleNames;

  public ExportsDirectiveTreeImpl(InternalSyntaxToken exportsKeyword, ExpressionTree packageName, @Nullable InternalSyntaxToken toKeyword, ListTreeImpl<TypeTree> moduleNames,
    InternalSyntaxToken semicolonToken) {
    super(Tree.Kind.EXPORTS_DIRECTIVE, exportsKeyword, semicolonToken);
    this.packageName = packageName;
    this.toKeyword = toKeyword;
    this.moduleNames = moduleNames;
  }

  @Override
  public void accept(TreeVisitor visitor) {
    visitor.visitExportsDirectiveTree(this);
  }

  @Override
  public Kind kind() {
    return Tree.Kind.EXPORTS_DIRECTIVE;
  }

  @Override
  public ExpressionTree packageName() {
    return packageName;
  }

  @Nullable
  @Override
  public SyntaxToken toKeyword() {
    return toKeyword;
  }

  @Override
  public ListTree<TypeTree> moduleNames() {
    return moduleNames;
  }

  @Override
  protected Iterable<Tree> children() {
    ImmutableList.Builder<Tree> iteratorBuilder = ImmutableList.<Tree>builder();
    iteratorBuilder.add(directiveKeyword(), packageName);
    if (toKeyword != null) {
      iteratorBuilder.add(toKeyword);
      iteratorBuilder.addAll(moduleNames.children());
    }
    iteratorBuilder.add(semicolonToken());
    return iteratorBuilder.build();
  }

}