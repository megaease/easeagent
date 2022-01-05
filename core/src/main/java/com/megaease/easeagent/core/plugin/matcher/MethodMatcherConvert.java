/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.core.plugin.matcher;

import com.megaease.easeagent.plugin.asm.Modifier;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import com.megaease.easeagent.plugin.matcher.operator.AndMethodMatcher;
import com.megaease.easeagent.plugin.matcher.operator.NegateMethodMatcher;
import com.megaease.easeagent.plugin.matcher.operator.OrMethodMatcher;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.NegatingMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class MethodMatcherConvert
    implements Converter<IMethodMatcher, Junction<MethodDescription>> {

    public static final MethodMatcherConvert INSTANCE = new MethodMatcherConvert();

    @Override
    public Junction<MethodDescription> convert(IMethodMatcher source) {
        if (source == null) {
            return null;
        }

        if (source instanceof AndMethodMatcher) {
            AndMethodMatcher andMatcher = (AndMethodMatcher) source;
            Junction<MethodDescription> leftMatcher = this.convert(andMatcher.getLeft());
            Junction<MethodDescription> rightMatcher = this.convert(andMatcher.getRight());
            return leftMatcher.and(rightMatcher);
        } else if (source instanceof OrMethodMatcher) {
            OrMethodMatcher andMatcher = (OrMethodMatcher) source;
            Junction<MethodDescription> leftMatcher = this.convert(andMatcher.getLeft());
            Junction<MethodDescription> rightMatcher = this.convert(andMatcher.getRight());
            return leftMatcher.or(rightMatcher);
        } else if (source instanceof NegateMethodMatcher) {
            NegateMethodMatcher matcher = (NegateMethodMatcher) source;
            Junction<MethodDescription> notMatcher = this.convert(matcher.getMatcher());
            return new NegatingMatcher<>(notMatcher);
        }

        if (!(source instanceof MethodMatcher)) {
            return null;
        }

        return this.convert((MethodMatcher) source);
    }

    private Junction<MethodDescription> convert(MethodMatcher matcher) {
        Junction<MethodDescription> c = null;
        if (matcher.getName() != null && matcher.getNameMatchType() != null) {
            switch (matcher.getNameMatchType()) {
                case EQUALS:
                    if ("<init>".equals(matcher.getName())) {
                        c = isConstructor();
                    } else {
                        c = named(matcher.getName());
                    }
                    break;
                case START_WITH:
                    c = nameStartsWith(matcher.getName());
                    break;
                case END_WITH:
                    c = nameEndsWith(matcher.getName());
                    break;
                case CONTAINS:
                    c = nameContains(matcher.getName());
                    break;
                default:
                    return null;
            }
        }

        Junction<MethodDescription> mc = fromModifier(matcher.getModifier(), false);
        if (mc != null) {
            c = c == null ? mc : c.and(mc);
        }
        mc = fromModifier(matcher.getNotModifier(), true);
        if (mc != null) {
            c = c == null ? mc : c.and(mc);
        }
        if (matcher.getReturnType() != null) {
            mc = returns(named(matcher.getReturnType()));
            c = c == null ? mc : c.and(mc);
        }
        if (matcher.getArgsLength() > -1) {
            mc = takesArguments(matcher.getArgsLength());
            c = c == null ? mc : c.and(mc);
        }
        String[] args = matcher.getArgs();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    mc = takesArgument(i, named(args[i]));
                    c = c == null ? mc : c.and(mc);
                }
            }
        }

        if (matcher.getArgsLength() >= 0) {
            mc = takesArguments(matcher.getArgsLength());
            c = c == null ? mc : c.and(mc);
        }

        if (matcher.getOverriddenFrom() != null) {
            mc = isOverriddenFrom(ClassMatcherConvert.INSTANCE.convert(matcher.getOverriddenFrom()));
            c = c == null ? mc : c.and(mc);
        }

        return c;
    }

    Junction<MethodDescription> fromModifier(int modifier, boolean not) {
        Junction<MethodDescription> mc = null;
        if ((modifier & ClassMatcher.MODIFIER_MASK) != 0) {
            if ((modifier & Modifier.ACC_ABSTRACT) != 0) {
                mc = isAbstract();
            }
            if ((modifier & Modifier.ACC_PUBLIC) != 0) {
                if (mc != null) {
                    mc = not ? mc.or(isPublic()) : mc.and(isPublic());
                } else {
                    mc = isPublic();
                }
            }
            if ((modifier & Modifier.ACC_PRIVATE) != 0) {
                if (mc != null) {
                    mc = not ? mc.or(isPrivate()) : mc.and(isPrivate());
                } else {
                    mc = isPrivate();
                }
            }
            if ((modifier & Modifier.ACC_PROTECTED) != 0) {
                if (mc != null) {
                    mc = not ? mc.or(isProtected()) : mc.and(isProtected());
                } else {
                    mc = isProtected();
                }
            }
            if ((modifier & Modifier.ACC_STATIC) != 0) {
                if (mc != null) {
                    mc = not ? mc.or(isStatic()) : mc.and(isStatic());
                } else {
                    mc = isStatic();
                }
            }
            if (not) {
                mc = new NegatingMatcher<>(mc);
            }
        }
        return mc;
    }
}
