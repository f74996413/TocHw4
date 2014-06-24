import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.*;


public class TocHw4 
{
	public static String UrlData(String httpAddress) 
	{
		URL URL = null;
		InputStream input = null;
		InputStreamReader read = null;
		BufferedReader buff = null;
		StringBuffer msg = null;

		try 
		{
			URL = new URL(httpAddress);
			input = URL.openStream();
			read = new InputStreamReader(input, "UTF-8");
			buff = new BufferedReader(read);

			String tmp_str = null;
			msg = new StringBuffer();

			while ((tmp_str = buff.readLine()) != null) 
			{
				msg.append(tmp_str);
			}
		} 
		catch (Exception e) 
		{
			e.getStackTrace();
		} 
		finally 
		{
			try 
			{
				URL = null;
				input.close();
				read.close();
				buff.close();
			} 
			catch (Exception e) 
			{

			}
		}
		return msg.toString();
	}

	public static String FileData(String filename)
	{
		StringBuffer msg = new StringBuffer();
		try 
		{
			InputStreamReader thefile = new InputStreamReader(new FileInputStream(filename), "UTF-8");
			BufferedReader BufRead = new BufferedReader(thefile);
			
			try 
			{
				do 
				{
					String buffer = BufRead.readLine();
					if (buffer == null)
					{
						break;
					}
					msg.append(buffer);
				} 
				while (true);
			} 
			catch (Exception e)
			{
			
			} 
			finally
			{
				BufRead.close();
			}
		}
		catch (Exception e)
		{
		
		}
		return msg.toString();
	}

	public static String detail(String input) 
	{
		char[] strings = {'路','街','巷'};
		int i = 0;
		
		for (char s : strings)
		{
			for (i = 0; i < input.length(); i++)
			{
				if (input.charAt(i) == s)
				{
					return input.substring(0, i++);
				}
			}
		}
		
		String pattern = "地號";
		Compare_Match ch = new Compare_Match(input, pattern);
		if (ch.match(0))
		{
			return input.substring(0 , ch.getmatch()+pattern.length());
		}
		
		pattern = "大道";
		ch = new Compare_Match(input , pattern);
		if (ch.match(0))
		{
			return input.substring(0 , ch.getmatch()+pattern.length());
		}
		
		pattern = "坐駕";
		ch = new Compare_Match(input, pattern);
		if (ch.match(0))
		{
			return input.substring(0 , ch.getmatch()+pattern.length());
		}
		
		pattern = "竹子腳";
		ch = new Compare_Match(input, pattern);
		if (ch.match(0))
		{
			return input.substring(0 , ch.getmatch()+pattern.length());
		}
		return null;
	}

	private static class info 
	{
		int dis_month = 0;
		ArrayList<Integer> list = new ArrayList<Integer>();

		int MAX = 0;
		int min = -1;

		public void add(int price, int month) throws Exception 
		{
			if (MAX < price)
			{
				MAX = price;
			}
			if (min > price || min == -1)
			{
				min = price;
			}
			if (!list.contains(new Integer(month)))
			{
				list.add(new Integer(month));
				dis_month++;
			}
		}
	}

	public static void main(String[] args) throws MalformedURLException, JSONException {

		HashMap<String, info> hashMap = new HashMap<String, info>();
		if (args.length != 1) 
		{
			System.out.println("Input Error...Sorry");
			return;
		}

		String URL = args[0];
		String contents = UrlData(URL);
		JSONArray JSONArray = new JSONArray(contents);

		int Max_dis_month = 0;
		
		ArrayList<String> houses = new ArrayList<String>();
		Integer arrival = 0;
		final HashMap<String , Integer> time = new HashMap<String , Integer>();
		
		for (int i = 0; i < JSONArray.length(); i++)
		{
			JSONObject ch = JSONArray.getJSONObject(i);
			info merchandise = null;
			String road = detail(ch.getString("土地區段位置或建物區門牌"));

			int price = ch.getInt("總價元");
			int month = ch.getInt("交易年月");
			try 
			{
				if ((merchandise = hashMap.get(road)) != null)
				{
					merchandise.add(price, month);
				}
				else
				{
					hashMap.put(road, merchandise = new info());
					arrival++;
					time.put(road, arrival);
					merchandise.add(price, month);
				}
				
				if (merchandise.dis_month > Max_dis_month) 
				{
					houses.clear();
					houses.add(road);
					Max_dis_month = merchandise.dis_month;
				}
				else if (merchandise.dis_month == Max_dis_month)
				{
					if (!houses.contains(road))
					{
						houses.add(road);
					}
				}

			} 
			catch (Exception e) 
			{
				
			}
		}
		Collections.sort(houses, new Comparator() 
		{
			public int compare(Object arg0, Object arg1) 
			{
				return time.get(arg0)-time.get(arg1);
			}
		});

		for (String s : houses)
			System.out.println(s + "路" + ", 最高成交價:" + hashMap.get(s).MAX + ", 最低成交價:" + hashMap.get(s).min);

	}

}


class Compare_Match 
{
	private String str;
	private String cmp_pattern;
	private int[] fail;
	private int match;
	private int ptr = 0;
	
	public Compare_Match(String str , String cmp_pattern)
	{
		this.str = str;
		this.cmp_pattern = cmp_pattern;
		fail = new int[cmp_pattern.length()];
		resultfail();
	}
	
	public int getmatch()
	{
		return match;
	}
	
	public int matchnumber()
	{
		int count = 0;
		while(continue_match())
		{
			count++;
		}
		return count;
	}
	
	public int ignore_match()
	{
		int count = 0;
		while(continue_ignore_match())
		{
			count++;
		}
		return count;
	}
	
	public boolean continue_ignore_match()
	{
		if(ignore(ptr))
		{
			ptr = match + cmp_pattern.length();
			return true;
		}
		ptr = 0;
		return false;
	}
	
	public boolean continue_match()
	{
		if(match(ptr))
		{
			ptr = match + cmp_pattern.length();
			return true;
		}
		
		ptr = 0;
		return false;
	}
	
	public boolean match(int i)
	{
		int tmp=0;
		if(str.length() == 0)
		{
			return false;
		}
		
		for(int count_i=i ; count_i<str.length() ; count_i++)
		{
			while(tmp>0 && cmp_pattern.charAt(tmp) != str.charAt(count_i))
			{
				tmp = fail[tmp-1];
			}
			
			if(cmp_pattern.charAt(tmp) == str.charAt(count_i))
			{
				tmp++;
			}
			
			if(tmp == cmp_pattern.length())
			{
				match = count_i - cmp_pattern.length() + 1;
				return true;
			}
		}
		return false;
	}
	
	public boolean ignore(int i)
	{
		int tmp=0;
		if(str.length() == 0)
		{
			return false;
		}
		
		for(int count_i=i ; count_i<str.length() ; count_i++)
		{
			while(tmp>0 && !errorcase(cmp_pattern.charAt(tmp) , str.charAt(count_i)))
			{
				tmp = fail[tmp-1];
			}
			
			if(errorcase(cmp_pattern.charAt(tmp), str.charAt(count_i)))
			{
				tmp++;
			}
			
			if(tmp==cmp_pattern.length())
			{
				match = count_i - cmp_pattern.length() + 1;
				return true;
			}
		}
		return false;
	}
	
	private boolean errorcase(char tmp_a , char tmp_b)
	{
		if(tmp_a == tmp_b)
		{
			return true;
		}
		if(tmp_a > tmp_b)
		{
			if(tmp_a-32 == tmp_b)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			if(tmp_b-32 == tmp_a)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	public boolean match_match()
	{
		int tmp_c = 0;
		int tmp_d = 0;
		
		if(str.length() == 0)
		{
			return false;
		}
		
		while(tmp_c+cmp_pattern.length()-tmp_d < str.length())
		{
			if(tmp_d >= cmp_pattern.length())
			{
				match = tmp_c - cmp_pattern.charAt(tmp_d);
				return true;
			}
			
			if(str.charAt(tmp_c) == cmp_pattern.charAt(tmp_d))
			{
				tmp_c = tmp_c + 1;
				tmp_d = tmp_d + 1;
			}
			else
			{
				if(tmp_d > 0)
				{
					tmp_d = fail[tmp_d - 1];
				}
				else
				{
					tmp_c = tmp_c + 1;
				}
			}
		}
		return false;
	}
	
	private void resultfail()
	{
		int tmp_d = 0;
		for(int tmp_c=1 ; tmp_c<cmp_pattern.length() ; tmp_c++)
		{
			while(tmp_d>0 && cmp_pattern.charAt(tmp_d) != cmp_pattern.charAt(tmp_c))
			{
				tmp_d = fail[tmp_d-1];
			}
			
			if(cmp_pattern.charAt(tmp_d) == cmp_pattern.charAt(tmp_c))
			{
				tmp_d = tmp_d + 1;
			}
			fail[tmp_c] = tmp_d;
		}
	}
}
