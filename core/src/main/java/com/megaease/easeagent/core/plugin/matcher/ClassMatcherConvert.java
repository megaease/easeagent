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
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.operator.AndClassMatcher;
import com.megaease.easeagent.plugin.matcher.operator.NotClassMatcher;
import com.megaease.easeagent.plugin.matcher.operator.OrClassMatcher;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.NegatingMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ClassMatcherConvert
    implements Converter<IClassMatcher, Junction<TypeDescription>> {
    @Override
    public Junction<TypeDescription> convert(IClassMatcher source) {
        if (source == null) {
            return null;
        }

        if (source instanceof AndClassMatcher) {
            AndClassMatcher andMatcher = (AndClassMatcher) source;
            Junction<TypeDescription> leftMatcher = this.convert(andMatcher.getLeft());
            Junction<TypeDescription> rightMatcher = this.convert(andMatcher.getLeft());
            return leftMatcher.and(rightMatcher);
        } else if (source instanceof OrClassMatcher) {
            OrClassMatcher andMatcher = (OrClassMatcher) source;
            Junction<TypeDescription> leftMatcher = this.convert(andMatcher.getLeft());
            Junction<TypeDescription> rightMatcher = this.convert(andMatcher.getLeft());
            return leftMatcher.or(rightMatcher);
        } else if (source instanceof NotClassMatcher) {
            NotClassMatcher matcher = (NotClassMatcher) source;
            Junction<TypeDescription> notMatcher = this.convert(matcher.getMatcher());
            return new NegatingMatcher<>(notMatcher);
        }

        if (!(source instanceof ClassMatcher)) {
            return null;
        }

        return this.convert((ClassMatcher) source);
    }

    private Junction<TypeDescription> convert(ClassMatcher matcher) {
        Junction<TypeDescription> c;
        switch (matcher.getMatchType()) {
            case NAMED:
                c = named(matcher.getName());
                break;
            case SUPER_CLASS:
            case INTERFACE:
                c = hasSuperType(named(matcher.getName()));
                break;
            case ANNOTATION:
                c = isAnnotatedWith(named(matcher.getName()));
                break;
            default:
                return null;
        }

        Junction<TypeDescription> mc = fromModifier(matcher.getModifier(), false);
        if (mc != null) {
            c = c.and(mc);
        }
        mc = fromModifier(matcher.getNotModifier(), true);
        if (mc != null) {
            c = c.and(mc);
        }

        // TODO: classloader matcher

        return c;
    }

    Junction<TypeDescription> fromModifier(int modifier, boolean not) {
        Junction<TypeDescription> mc = null;
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
            if (not) {
                mc = new NegatingMatcher<>(mc);
            }
        }
        return mc;
    }
}
