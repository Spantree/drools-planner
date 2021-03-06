/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.core.move.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.drools.planner.core.domain.variable.PlanningVariableDescriptor;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.score.director.ScoreDirector;

public class GenericReverseChainedChangePartMove implements Move {

    private final List<Object> entitiesSubChain;
    private final Object firstEntity;
    private final Object lastEntity;
    private final PlanningVariableDescriptor planningVariableDescriptor;
    private final Object toPlanningValue;
    private final Object oldTrailingEntity;
    private final Object newTrailingEntity;

    public GenericReverseChainedChangePartMove(List<Object> entitiesSubChain,
            PlanningVariableDescriptor planningVariableDescriptor, Object toPlanningValue,
            Object oldTrailingEntity, Object newTrailingEntity) {
        this.entitiesSubChain = entitiesSubChain;
        this.planningVariableDescriptor = planningVariableDescriptor;
        this.toPlanningValue = toPlanningValue;
        this.oldTrailingEntity = oldTrailingEntity;
        this.newTrailingEntity = newTrailingEntity;
        firstEntity = this.entitiesSubChain.get(0);
        lastEntity = this.entitiesSubChain.get(entitiesSubChain.size() - 1);
    }

    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return true; // Done by GenericChainedChangePartMoveFactory
    }

    public Move createUndoMove(ScoreDirector scoreDirector) {
        Object oldFirstPlanningValue = planningVariableDescriptor.getValue(firstEntity);
        ArrayList<Object> reversedEntitiesSubChain = new ArrayList<Object>(entitiesSubChain);
        Collections.reverse(reversedEntitiesSubChain);
        return new GenericReverseChainedChangePartMove(reversedEntitiesSubChain,
                planningVariableDescriptor, oldFirstPlanningValue,
                newTrailingEntity, oldTrailingEntity);
    }

    public void doMove(ScoreDirector scoreDirector) {
        Object oldFirstPlanningValue = planningVariableDescriptor.getValue(firstEntity);
        if (firstEntity.equals(newTrailingEntity)) {
            // Unmoved reverse
            // Temporary close the old chain
            if (oldTrailingEntity != null) {
                scoreDirector.beforeVariableChanged(oldTrailingEntity, planningVariableDescriptor.getVariableName());
                planningVariableDescriptor.setValue(oldTrailingEntity, oldFirstPlanningValue);
                scoreDirector.afterVariableChanged(oldTrailingEntity, planningVariableDescriptor.getVariableName());
            }
        } else {
            // Close the old chain
            if (oldTrailingEntity != null) {
                scoreDirector.beforeVariableChanged(oldTrailingEntity, planningVariableDescriptor.getVariableName());
                planningVariableDescriptor.setValue(oldTrailingEntity, oldFirstPlanningValue);
                scoreDirector.afterVariableChanged(oldTrailingEntity, planningVariableDescriptor.getVariableName());
            }
        }
        // Change the entity
        Object nextEntity = toPlanningValue;
        for (ListIterator<Object> it = entitiesSubChain.listIterator(entitiesSubChain.size()); it.hasPrevious();) {
            Object entity = it.previous();
            scoreDirector.beforeVariableChanged(entity, planningVariableDescriptor.getVariableName());
            planningVariableDescriptor.setValue(entity, nextEntity);
            scoreDirector.afterVariableChanged(entity, planningVariableDescriptor.getVariableName());
            nextEntity = entity;
        }
        if (firstEntity.equals(newTrailingEntity)) {
            // Unmoved reverse
            // Reroute the old chain
            if (oldTrailingEntity != null) {
                scoreDirector.beforeVariableChanged(oldTrailingEntity, planningVariableDescriptor.getVariableName());
                planningVariableDescriptor.setValue(oldTrailingEntity, firstEntity);
                scoreDirector.afterVariableChanged(oldTrailingEntity, planningVariableDescriptor.getVariableName());
            }
        } else {
            // Reroute the new chain
            if (newTrailingEntity != null) {
                scoreDirector.beforeVariableChanged(newTrailingEntity, planningVariableDescriptor.getVariableName());
                planningVariableDescriptor.setValue(newTrailingEntity, firstEntity);
                scoreDirector.afterVariableChanged(newTrailingEntity, planningVariableDescriptor.getVariableName());
            }
        }
    }

    public Collection<? extends Object> getPlanningEntities() {
        return entitiesSubChain;
    }

    public Collection<? extends Object> getPlanningValues() {
        return Collections.singletonList(toPlanningValue);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof GenericReverseChainedChangePartMove) {
            GenericReverseChainedChangePartMove other = (GenericReverseChainedChangePartMove) o;
            return new EqualsBuilder()
                    .append(entitiesSubChain, other.entitiesSubChain)
                    .append(planningVariableDescriptor.getVariableName(),
                            other.planningVariableDescriptor.getVariableName())
                    .append(toPlanningValue, other.toPlanningValue)
                    .isEquals();
        } else {
            return false;
        }
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(entitiesSubChain)
                .append(planningVariableDescriptor.getVariableName())
                .append(toPlanningValue)
                .toHashCode();
    }

    public String toString() {
        return "Reversed " + entitiesSubChain + " => " + toPlanningValue;
    }

}
