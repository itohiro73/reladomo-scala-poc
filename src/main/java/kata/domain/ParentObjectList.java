package kata.domain;
import com.gs.fw.finder.Operation;
import java.util.*;
public class ParentObjectList extends ParentObjectListAbstract
{
	public ParentObjectList()
	{
		super();
	}

	public ParentObjectList(int initialSize)
	{
		super(initialSize);
	}

	public ParentObjectList(Collection c)
	{
		super(c);
	}

	public ParentObjectList(Operation operation)
	{
		super(operation);
	}
}
