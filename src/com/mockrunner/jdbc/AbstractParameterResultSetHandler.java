package com.mockrunner.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mockrunner.mock.jdbc.MockResultSet;
import com.mockrunner.util.common.ArrayUtil;

/**
 * Abstract base class for all statement types
 * that support parameters, i.e. <code>PreparedStatement</code>
 * and <code>CallableStatement</code>.
 */
public abstract class AbstractParameterResultSetHandler extends AbstractResultSetHandler
{
    private boolean exactMatchParameter = false;
    private Map resultSetsForStatement = new HashMap();
    private Map updateCountForStatement = new HashMap();
    private Map throwsSQLException = new HashMap();
	private Map executedStatementParameters = new HashMap();
    
	/**
	 * Collects all SQL strings that were executed.
	 * @param sql the SQL string
	 * @param parameters a copy of the corresponding parameter map
	 */
	public void addParameterMapForExecutedStatement(String sql, Map parameters)
	{
		if(null != parameters)
		{
			if(null == executedStatementParameters.get(sql))
			{
				executedStatementParameters.put(sql, new ParameterSets(sql));
			}
			ParameterSets sets = (ParameterSets)executedStatementParameters.get(sql);
			sets.addParameterSet(parameters);
		}
	}
	
	/**
	 * Returns the <code>ParameterSets</code> for a specified
	 * SQL string.
	 * @param sql the SQL string
	 * @return the <code>Map</code> of parameters
	 */
	public ParameterSets getParametersForExecutedStatement(String sql)
	{
		return (ParameterSets)executedStatementParameters.get(sql);
	}
	
	/**
	 * Returns the <code>Map</code> of executed SQL strings.
	 * Each string maps to the corresponding {@link ParameterSets}
	 * object.
	 * @return the <code>Map</code> of parameters
	 */
	public Map getExecutedStatementParameter()
	{
		return Collections.unmodifiableMap(executedStatementParameters);
	}
    
    /**
     * Sets if the specified parameters must match exactly
     * in order and number.
     * Defaults to <code>false</code>, i.e. the specified
     * parameters must be present in the actual parameter
     * list of the prepared statement with the correct index
     * but it's ok if there are more actual parameters.
     * @param exactMatchParameter must parameters match exactly
     */
    public void setExactMatchParameter(boolean exactMatchParameter)
    {
        this.exactMatchParameter = exactMatchParameter;
    }

    /**
     * Returns the first update count that matches the
     * specified SQL string and the specified parameters. 
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param parameters the parameters
     * @return the corresponding update count
     */
    public Integer getUpdateCount(String sql, Map parameters)
    {
        Integer[] updateCounts = getUpdateCounts(sql, parameters);
        if(null != updateCounts && updateCounts.length > 0)
        {
            return updateCounts[0];
        }
        return null;
    }
    
    public Integer[] getUpdateCounts(String sql, Map parameters)
    {
        ParameterWrapper wrapper = (ParameterWrapper)getMatchingParameterWrapper(sql, parameters, updateCountForStatement);
        if(null != wrapper)
        {
            if(wrapper instanceof MockUpdateCountWrapper)
            {
                return new Integer[] {((MockUpdateCountWrapper)wrapper).getUpdateCount()};
            }
            else if(wrapper instanceof MockUpdateCountArrayWrapper)
            {
                return ((MockUpdateCountArrayWrapper)wrapper).getUpdateCount();
            }
        }
        return null;
    }
    
    public boolean hasMultipleUpdateCounts(String sql, Map parameters)
    {
        ParameterWrapper wrapper = (ParameterWrapper)getMatchingParameterWrapper(sql, parameters, updateCountForStatement);
        return (wrapper instanceof MockUpdateCountArrayWrapper);
    }

    /**
     * Returns the first <code>ResultSet</code> that matches the
     * specified SQL string and the specified parameters.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param parameters the parameters
     * @return the corresponding {@link MockResultSet}
     */
    public MockResultSet getResultSet(String sql, Map parameters)
    {
        MockResultSet[] resultSets = getResultSets(sql, parameters);
        if(null != resultSets && resultSets.length > 0)
        {
            return resultSets[0];
        }
        return null;
    }
    
    public MockResultSet[] getResultSets(String sql, Map parameters)
    {
        ParameterWrapper wrapper = (ParameterWrapper)getMatchingParameterWrapper(sql, parameters, resultSetsForStatement);
        if(null != wrapper)
        {
            if(wrapper instanceof MockResultSetWrapper)
            {
                return new MockResultSet[] {((MockResultSetWrapper)wrapper).getResultSet()};
            }
            else if(wrapper instanceof MockResultSetArrayWrapper)
            {
                return ((MockResultSetArrayWrapper)wrapper).getResultSets();
            }
        }
        return null;
    }
    
    public boolean hasMultipleResultSets(String sql, Map parameters)
    {
        ParameterWrapper wrapper = (ParameterWrapper)getMatchingParameterWrapper(sql, parameters, resultSetsForStatement);
        return (wrapper instanceof MockResultSetArrayWrapper);
    }
    
    /**
     * Returns if the specified SQL string with the specified parameters
     * should raise an exception.
     * This can be used to simulate database exceptions.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param parameters the parameters
     * @return <code>true</code> if the specified SQL string should raise an exception,
     *         <code>false</code> otherwise
     */
    public boolean getThrowsSQLException(String sql, Map parameters)
    {
        return (getSQLException(sql, parameters) != null);
    }
    
    /**
     * Returns the <code>SQLException</code> the specified SQL string
     * should throw. Returns <code>null</code> if the specified SQL string
     * should not throw an exception.
     * This can be used to simulate database exceptions.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param parameters the parameters
     * @return the <code>SQLException</code> or <code>null</code>
     */
    public SQLException getSQLException(String sql, Map parameters)
    {
        MockSQLExceptionWrapper wrapper = (MockSQLExceptionWrapper)getMatchingParameterWrapper(sql, parameters, throwsSQLException);
        if(null != wrapper)
        {
            return wrapper.getException();
        }
        return null;
    }

    protected ParameterWrapper getMatchingParameterWrapper(String sql, Map parameters, Map statementMap)
    {
        SQLStatementMatcher matcher = new SQLStatementMatcher(getCaseSensitive(), getExactMatch(), getUseRegularExpressions());
        List list = matcher.getMatchingObjects(statementMap, sql, true, true);
        for(int ii = 0; ii < list.size(); ii++)
        {
            ParameterWrapper wrapper = (ParameterWrapper)list.get(ii);
            if(doParameterMatch(wrapper.getParamters(), parameters))
            {
                return wrapper;
            }
        }
        return null;
    }
    
    private boolean doParameterMatch(Map expectedParameters, Map actualParameters)
    {
        if(exactMatchParameter)
        {
            if(actualParameters.size() != expectedParameters.size()) return false;
            Iterator iterator = actualParameters.keySet().iterator();
            while(iterator.hasNext())
            {
                Object currentKey = iterator.next();
                if(!actualParameters.containsKey(currentKey)) return false;
                Object expectedObject = expectedParameters.get(currentKey);
                if(!ParameterUtil.compareParameter(actualParameters.get(currentKey), expectedObject))
                {
                    return false;
                }
            }
            return true;
        }
        else
        {
            Iterator iterator = expectedParameters.keySet().iterator();
            while(iterator.hasNext())
            {
                Object currentKey = iterator.next();
                if(!actualParameters.containsKey(currentKey)) return false;
                Object actualObject = actualParameters.get(currentKey);
                if(!ParameterUtil.compareParameter(actualObject, expectedParameters.get(currentKey)))
                {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Clears the <code>ResultSet</code> objects.
     */
    public void clearResultSets()
    {
        super.clearResultSets();
        resultSetsForStatement.clear();
    }
    
    /**
     * Clears the update counts.
     */
    public void clearUpdateCounts()
    {
        super.clearUpdateCounts();
        updateCountForStatement.clear();
    }
    
    /**
     * Clears the list of statements that should throw an exception
     */
    public void clearThrowsSQLException()
    {
        super.clearThrowsSQLException();
        throwsSQLException.clear();
    }

    /**
     * Prepare a <code>ResultSet</code> for a specified SQL string and
     * the specified parameters. The specified parameters array
     * must contain the parameters in the correct order starting with index 0 for
     * the first parameter. Please keep in mind that parameters in
     * <code>PreparedStatement</code> objects start with 1 as the first
     * parameter. So <code>parameters[0]</code> maps to the
     * parameter with index 1.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param resultSet the corresponding {@link MockResultSet}
     * @param parameters the parameters
     */
    public void prepareResultSet(String sql, MockResultSet resultSet, Object[] parameters)
    {
        prepareResultSet(sql, resultSet, ArrayUtil.getListFromObjectArray(parameters));
    }
    
    public void prepareResultSets(String sql, MockResultSet[] resultSets, Object[] parameters)
    {
        prepareResultSets(sql, resultSets, ArrayUtil.getListFromObjectArray(parameters));
    }

    /**
     * Prepare a <code>ResultSet</code> for a specified SQL string and
     * the specified parameters. The specified parameters <code>List</code>
     * must contain the parameters in the correct order starting with index 0 for
     * the first parameter. Please keep in mind that parameters in
     * <code>PreparedStatement</code> objects start with 1 as the first
     * parameter. So <code>parameters.get(0)</code> maps to the
     * parameter with index 1.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param resultSet the corresponding {@link MockResultSet}
     * @param parameters the parameters
     */
    public void prepareResultSet(String sql, MockResultSet resultSet, List parameters)
    {
        Map params = createParameterMap(parameters);
        prepareResultSet(sql, resultSet, params);
    }
    
    public void prepareResultSets(String sql, MockResultSet[] resultSets, List parameters)
    {
        Map params = createParameterMap(parameters);
        prepareResultSets(sql, resultSets, params);
    }
    
    /**
     * Prepare a <code>ResultSet</code> for a specified SQL string and
     * the specified parameters. The specified parameters <code>Map</code>
     * must contain the parameters by mapping <code>Integer</code> objects
     * to the corresponding parameter. The <code>Integer</code> object
     * is the index of the parameter. In the case of a <code>CallableStatement</code>
     * there are also allowed <code>String</code> keys for named parameters.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param resultSet the corresponding {@link MockResultSet}
     * @param parameters the parameters
     */
    public void prepareResultSet(String sql, MockResultSet resultSet, Map parameters)
    {
        List list = getListFromMapForSQLStatement(sql, resultSetsForStatement);
        list.add(new MockResultSetWrapper(resultSet, new HashMap(parameters)));
    }
    
    public void prepareResultSets(String sql, MockResultSet[] resultSets, Map parameters)
    {
        List list = getListFromMapForSQLStatement(sql, resultSetsForStatement);
        list.add(new MockResultSetArrayWrapper((MockResultSet[])resultSets.clone(), new HashMap(parameters)));
    }
    
    /**
     * Prepare that the specified SQL string with the specified parameters
     * should raise an exception.
     * This can be used to simulate database exceptions.
     * This method creates an <code>SQLException</code> and will throw this 
     * exception. With {@link #prepareThrowsSQLException(String, SQLException, Object[])} 
     * you can specify the exception.
     * The specified parameters array must contain the parameters in 
     * the correct order starting with index 0 for the first parameter. 
     * Please keep in mind that parameters in <code>PreparedStatement</code> 
     * objects start with 1 as the first parameter. So <code>parameters[0]</code> 
     * maps to the parameter with index 1.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param parameters the parameters
     */
    public void prepareThrowsSQLException(String sql, Object[] parameters)
    {
        SQLException exc = new SQLException("Statement " + sql + " was specified to throw an exception");
        prepareThrowsSQLException(sql, exc, parameters);
    }
    
    /**
     * Prepare that the specified SQL string with the specified parameters
     * should raise an exception.
     * This can be used to simulate database exceptions.
     * This method creates an <code>SQLException</code> and will throw this 
     * exception. With {@link #prepareThrowsSQLException(String, SQLException, List)} 
     * you can specify the exception.
     * The specified parameters <code>List</code> must contain the 
     * parameters in the correct order starting with index 0 for the first 
     * parameter. Please keep in mind that parameters in 
     * <code>PreparedStatement</code> objects start with 1 as the first
     * parameter. So <code>parameters.get(0)</code> maps to the parameter 
     * with index 1.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param parameters the parameters
     */
    public void prepareThrowsSQLException(String sql, List parameters)
    {
        SQLException exc = new SQLException("Statement " + sql + " was specified to throw an exception");
        prepareThrowsSQLException(sql, exc, parameters);
    }
    
    /**
     * Prepare that the specified SQL string with the specified parameters
     * should raise an exception.
     * This can be used to simulate database exceptions.
     * This method creates an <code>SQLException</code> and will throw this 
     * exception. With {@link #prepareThrowsSQLException(String, SQLException, Map)} 
     * you can specify the exception.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param parameters the parameters
     */
    public void prepareThrowsSQLException(String sql, Map parameters)
    {
        SQLException exc = new SQLException("Statement " + sql + " was specified to throw an exception");
        prepareThrowsSQLException(sql, exc, parameters);
    }
    
    /**
     * Prepare that the specified SQL string with the specified parameters
     * should raise an exception.
     * This can be used to simulate database exceptions.
     * This method takes an exception object that will be thrown.
     * The specified parameters array must contain the parameters in 
     * the correct order starting with index 0 for the first parameter. 
     * Please keep in mind that parameters in <code>PreparedStatement</code> 
     * objects start with 1 as the first parameter. So <code>parameters[0]</code> 
     * maps to the parameter with index 1.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param exc the <code>SQLException</code> that should be thrown
     * @param parameters the parameters
     */
    public void prepareThrowsSQLException(String sql, SQLException exc, Object[] parameters)
    {
        prepareThrowsSQLException(sql, exc, ArrayUtil.getListFromObjectArray(parameters));
    }
    
    /**
     * Prepare that the specified SQL string with the specified parameters
     * should raise an exception.
     * This can be used to simulate database exceptions.
     * This method takes an exception object that will be thrown.
     * The specified parameters <code>List</code> must contain the 
     * parameters in the correct order starting with index 0 for the first 
     * parameter. Please keep in mind that parameters in 
     * <code>PreparedStatement</code> objects start with 1 as the first
     * parameter. So <code>parameters.get(0)</code> maps to the parameter 
     * with index 1.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param exc the <code>SQLException</code> that should be thrown
     * @param parameters the parameters
     */
    public void prepareThrowsSQLException(String sql, SQLException exc, List parameters)
    {
        Map params = createParameterMap(parameters);
        prepareThrowsSQLException(sql, exc, params);
    }
    
    /**
     * Prepare that the specified SQL string with the specified parameters
     * should raise an exception.
     * This can be used to simulate database exceptions.
     * This method takes an exception object that will be thrown.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param exc the <code>SQLException</code> that should be thrown
     * @param parameters the parameters
     */
    public void prepareThrowsSQLException(String sql, SQLException exc, Map parameters)
    {
        List list = getListFromMapForSQLStatement(sql, throwsSQLException);
        list.add(new MockSQLExceptionWrapper(exc, new HashMap(parameters)));
    }

    /**
     * Prepare the update count for execute update calls for a specified SQL string
     * and the specified parameters. The specified parameters array
     * must contain the parameters in the correct order starting with index 0 for
     * the first parameter. Please keep in mind that parameters in
     * <code>PreparedStatement</code> objects start with 1 as the first
     * parameter. So <code>parameters[0]</code> maps to the
     * parameter with index 1.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param updateCount the update count
     * @param parameters the parameters
     */
    public void prepareUpdateCount(String sql, int updateCount, Object[] parameters)
    {
        prepareUpdateCount(sql, updateCount, ArrayUtil.getListFromObjectArray(parameters));
    }
    
    public void prepareUpdateCounts(String sql, int[] updateCounts, Object[] parameters)
    {
        prepareUpdateCounts(sql, updateCounts, ArrayUtil.getListFromObjectArray(parameters));
    }

    /**
     * Prepare the update count for execute update calls for a specified SQL string
     * and the specified parameters. The specified parameters <code>List</code>
     * must contain the parameters in the correct order starting with index 0 for
     * the first parameter. Please keep in mind that parameters in
     * <code>PreparedStatement</code> objects start with 1 as the first
     * parameter. So <code>parameters.get(0)</code> maps to the
     * parameter with index 1.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param updateCount the update count
     * @param parameters the parameters
     */
    public void prepareUpdateCount(String sql, int updateCount, List parameters)
    {
        Map params = createParameterMap(parameters);
        prepareUpdateCount(sql, updateCount,  params);
    }
    
    public void prepareUpdateCounts(String sql, int[] updateCounts, List parameters)
    {
        Map params = createParameterMap(parameters);
        prepareUpdateCounts(sql, updateCounts,  params);
    }
    
    /**
     * Prepare the update count for execute update calls for a specified SQL string
     * and the specified parameters. The specified parameters <code>Map</code>
     * must contain the parameters by mapping <code>Integer</code> objects
     * to the corresponding parameter. The <code>Integer</code> object
     * is the index of the parameter. In the case of a <code>CallableStatement</code>
     * there are also allowed <code>String</code> keys for named parameters.
     * Please note that you can modify the match parameters with 
     * {@link #setCaseSensitive}, {@link #setExactMatch} and 
     * {@link #setUseRegularExpressions} and the match parameters for the 
     * specified parameter list with {@link #setExactMatchParameter}.
     * @param sql the SQL string
     * @param updateCount the update count
     * @param parameters the parameters
     */
    public void prepareUpdateCount(String sql, int updateCount, Map parameters)
    {
        List list = getListFromMapForSQLStatement(sql, updateCountForStatement);
        list.add(new MockUpdateCountWrapper(updateCount, new HashMap(parameters)));
    }
    
    public void prepareUpdateCounts(String sql, int[] updateCounts, Map parameters)
    {
        List list = getListFromMapForSQLStatement(sql, updateCountForStatement);
        list.add(new MockUpdateCountArrayWrapper((int[])updateCounts.clone(), new HashMap(parameters)));
    }
    
    private List getListFromMapForSQLStatement(String sql, Map map)
    {
        List list = (List)map.get(sql);
        if(null == list)
        {
            list = new ArrayList();
            map.put(sql, list);
        }
        return list;
    }
    
    private Map createParameterMap(List parameters)
    {
        Map params = new HashMap();
        for(int ii = 0; ii < parameters.size(); ii++)
        {
            params.put(new Integer(ii + 1), parameters.get(ii));
        }
        return params;
    }
    
    protected class ParameterWrapper
    {
        private Map parameters;
        
        public ParameterWrapper(Map parameters)
        {
            this.parameters = parameters;
        }

        public Map getParamters()
        {
            return parameters;
        }
    }
    
    private class MockSQLExceptionWrapper extends ParameterWrapper
    {
        private SQLException exception;
        
    
        public MockSQLExceptionWrapper(SQLException exception, Map parameters)
        {
            super(parameters);
            this.exception = exception;
        }

        public SQLException getException()
        {
            return exception;
        }
    }
    
    private class MockResultSetWrapper extends ParameterWrapper
    {
        private MockResultSet resultSet;
    
        public MockResultSetWrapper(MockResultSet resultSet, Map parameters)
        {
            super(parameters);
            this.resultSet = resultSet;
        }

        public MockResultSet getResultSet()
        {
            return resultSet;
        }
    }
    
    private class MockResultSetArrayWrapper extends ParameterWrapper
    {
        private MockResultSet[] resultSets;
    
        public MockResultSetArrayWrapper(MockResultSet[] resultSets, Map parameters)
        {
            super(parameters);
            this.resultSets = resultSets;
        }

        public MockResultSet[] getResultSets()
        {
            return resultSets;
        }
    }

    private class MockUpdateCountWrapper extends ParameterWrapper
    {
        private Integer updateCount;

        public MockUpdateCountWrapper(int updateCount, Map parameters)
        {
            super(parameters);
            this.updateCount = new Integer(updateCount);
        }

        public Integer getUpdateCount()
        {
            return updateCount;
        }
    }
    
    private class MockUpdateCountArrayWrapper extends ParameterWrapper
    {
        private Integer[] updateCounts;

        public MockUpdateCountArrayWrapper(int[] updateCounts, Map parameters)
        {
            super(parameters);
            this.updateCounts = (Integer[])ArrayUtil.convertToObjectArray(updateCounts);
        }

        public Integer[] getUpdateCount()
        {
            return updateCounts;
        }
    }
}
