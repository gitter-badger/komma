package net.enilink.komma.internal.sesame.result;

import java.util.List;

import net.enilink.komma.core.IBindings;
import net.enilink.komma.core.ITupleResult;
import net.enilink.komma.core.IValue;
import net.enilink.komma.core.LinkedHashBindings;
import net.enilink.komma.sesame.SesameValueConverter;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import com.google.inject.Inject;

public class SesameTupleResult extends
		SesameResult<BindingSet, IBindings<IValue>> implements
		ITupleResult<IBindings<IValue>> {
	protected SesameValueConverter valueConverter;

	public SesameTupleResult(TupleQueryResult result) {
		super(result);
	}

	@Override
	protected IBindings<IValue> convert(BindingSet element) throws Exception {
		LinkedHashBindings<IValue> result = new LinkedHashBindings<IValue>(
				element.size());
		for (String name : getBindingNames()) {
			IValue value = valueConverter.fromSesame(element.getValue(name));
			if (value != null) {
				result.put(name, value);
			}
		}
		return result;
	}

	@Override
	public List<String> getBindingNames() {
		try {
			return ((TupleQueryResult) delegate).getBindingNames();
		} catch (QueryEvaluationException e) {
			throw new RuntimeException(e);
		}
	}

	@Inject
	public void setValueConverter(SesameValueConverter valueConverter) {
		this.valueConverter = valueConverter;
	}
}