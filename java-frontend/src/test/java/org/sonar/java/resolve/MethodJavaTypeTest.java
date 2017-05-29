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
package org.sonar.java.resolve;

import org.junit.Test;
import org.sonar.java.bytecode.loader.SquidClassLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodJavaTypeTest {

  private static final ParametrizedTypeCache parametrizedTypeCache = new ParametrizedTypeCache();
  private static final BytecodeCompleter bytecodeCompleter = new BytecodeCompleter(new SquidClassLoader(Collections.emptyList()), parametrizedTypeCache);
  private static final Symbols SYMBOLS = new Symbols(bytecodeCompleter);
  // only used to correctly initialize the parameterized type cache with a type substitution solver
  private static final TypeSubstitutionSolver typeSubstitutionSolver = new TypeSubstitutionSolver(parametrizedTypeCache, SYMBOLS);

  private static final JavaSymbol.TypeJavaSymbol MY_TYPE = new JavaSymbol.TypeJavaSymbol(Flags.PUBLIC, "MyType", SYMBOLS.defaultPackage);

  @Test
  public void return_type() {
    MethodJavaType methodJavaType = method(SYMBOLS.intType);
    assertThat(methodJavaType.resultType()).isSameAs(SYMBOLS.intType);

    MethodJavaType constructor = constructor();
    assertThat(constructor.resultType()).isNull();
  }

  @Test
  public void to_string_on_type() throws Exception {
    assertThat(new JavaType(JavaType.VOID, null).toString()).isEmpty();

    String methodToString = method(SYMBOLS.intType).toString();
    assertThat(methodToString).isEqualTo("returns int");

    String constructorToString = constructor().toString();
    assertThat(constructorToString).isEqualTo("constructor");
  }

  @Test
  public void thrown_types() {
    MethodJavaType m1 = methodWithThrownExceptions(Collections.emptyList());
    assertThat(m1.thrownTypes()).isEmpty();

    MethodJavaType m2 = methodWithThrownExceptions(Collections.singletonList(Symbols.unknownType));
    assertThat(m2.thrownTypes()).hasSize(1);
    assertThat(m2.thrownTypes()).containsOnly(Symbols.unknownType);

    JavaType t1 = new JavaType(JavaType.CLASS, Symbols.unknownSymbol);
    JavaType t2 = new JavaType(JavaType.CLASS, Symbols.unknownSymbol);
    MethodJavaType m3 = methodWithThrownExceptions(Arrays.asList(t1, t2));
    assertThat(m3.thrownTypes()).hasSize(2);
    assertThat(m3.thrownTypes()).containsExactly(t1, t2);
  }

  @Test
  public void arg_types() {
    MethodJavaType m1 = methodWithArgs(Collections.emptyList());
    assertThat(m1.argTypes()).isEmpty();

    MethodJavaType m2 = methodWithArgs(Collections.singletonList(Symbols.unknownType));
    assertThat(m2.argTypes()).hasSize(1);
    assertThat(m2.argTypes()).containsOnly(Symbols.unknownType);

    JavaType t1 = new JavaType(JavaType.CLASS, Symbols.unknownSymbol);
    JavaType t2 = new JavaType(JavaType.CLASS, Symbols.unknownSymbol);
    MethodJavaType m3 = methodWithArgs(Arrays.asList(t1, t2));
    assertThat(m3.argTypes()).hasSize(2);
    assertThat(m3.argTypes()).containsExactly(t1, t2);
  }

  @Test
  public void is_uses_return_type_for_methods() {
    MethodJavaType m = method(SYMBOLS.intType.primitiveWrapperType);
    assertThat(m.is("java.lang.Integer")).isTrue();
    assertThat(m.is("int")).isFalse();

    MethodJavaType c = constructor();
    assertThat(c.is("int")).isFalse();
  }

  @Test
  public void isSubtypeOf_String_uses_return_type_for_methods() {
    MethodJavaType m = method(SYMBOLS.intType.primitiveWrapperType);
    assertThat(m.isSubtypeOf("java.lang.Number")).isTrue();
    assertThat(m.isSubtypeOf("java.lang.String")).isFalse();

    MethodJavaType c = constructor();
    assertThat(c.isSubtypeOf("java.lang.String")).isFalse();
  }

  @Test
  public void isSubtypeOf_Type_uses_return_type_for_methods() {
    MethodJavaType m = method(SYMBOLS.intType.primitiveWrapperType);
    assertThat(m.isSubtypeOf(SYMBOLS.objectType)).isTrue();
    assertThat(m.isSubtypeOf(SYMBOLS.serializableType)).isTrue();
    assertThat(m.isSubtypeOf(SYMBOLS.stringType)).isFalse();

    MethodJavaType c = constructor();
    assertThat(c.isSubtypeOf(SYMBOLS.stringType)).isFalse();
  }

  private static MethodJavaType constructor() {
    return new MethodJavaType(Collections.emptyList(), null, Collections.emptyList(), MY_TYPE);
  }

  private static MethodJavaType method(JavaType returnType) {
    return new MethodJavaType(Collections.emptyList(), returnType, Collections.emptyList(), MY_TYPE);
  }

  private static MethodJavaType methodWithArgs(List<JavaType> arguments) {
    return new MethodJavaType(arguments, SYMBOLS.voidType, Collections.emptyList(), MY_TYPE);
  }

  private static MethodJavaType methodWithThrownExceptions(List<JavaType> thrown) {
    return new MethodJavaType(Collections.emptyList(), SYMBOLS.voidType, thrown, MY_TYPE);
  }
}
