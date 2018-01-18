package org.hisp.dhis.rules.functions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import static java.util.Arrays.asList;

import org.hisp.dhis.rules.RuleVariableValue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RuleFunctionOizpShould {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Map<String, RuleVariableValue> variableValues = new HashMap<>();

    @Test
    public void return_one_for_non_negative_argument() {
        RuleFunction oizp = RuleFunctionOizp.create();

        assertThat(oizp.evaluate(asList("0"), variableValues), is("1.0"));
        assertThat(oizp.evaluate(asList("1"), variableValues), is("1.0"));
        assertThat(oizp.evaluate(asList("10"), variableValues), is("1.0"));
    }

    @Test
    public void return_zero_for_negative_argument() {
        RuleFunction oizp = RuleFunctionOizp.create();

        assertThat(oizp.evaluate(asList("-1"), variableValues), is("0.0"));
        assertThat(oizp.evaluate(asList("-10"), variableValues), is("0.0"));
    }

    @Test
    public void return_zero_for_non_number_argument() {
        RuleFunction oizp = RuleFunctionOizp.create();

        assertThat(oizp.evaluate(asList("non_number"), variableValues), is("0.0"));
    }

    @Test
    public void throw_illegal_argument_exception_when_argument_count_is_greater_than_expected() {
        thrown.expect(IllegalArgumentException.class);
        RuleFunctionOizp.create().evaluate(Arrays.asList("5.9", "6.8"), variableValues);
    }

    @Test
    public void throw_illegal_argument_exception_when_arguments_count_is_lower_than_expected() {
        thrown.expect(IllegalArgumentException.class);
        RuleFunctionOizp.create().evaluate(new ArrayList<String>(), variableValues);
    }

    @Test
    public void throw_illegal_argument_exception_when_arguments_is_null() {
        thrown.expect(IllegalArgumentException.class);
        RuleFunctionOizp.create().evaluate(null, variableValues);
    }
}
