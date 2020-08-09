import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Test {

	// 초성
	public static final String[] ENG_MAPPING_CHOSUNG = { "r", "R", "s", "e", "E", "f", "a", "q", "Q", "t", "T", "d",
			"w", "W", "c", "z", "x", "v", "g" };

	// 중성
	public static final String[] ENG_MAPPING_JUNGSUNG = { "k", "o", "i", "O", "j", "p", "u", "P", "h", "hk", "ho", "hl",
			"y", "n", "nj", "np", "nl", "b", "m", "ml", "l" };

	// 종성
	public static final String[] ENG_MAPPING_JONGSUNG = { "r", "R", "rt", "s", "sw", "sg", "e", "f", "fr", "fa", "fq",
			"ft", "fx", "fv", "fg", "a", "q", "qt", "t", "T", "d", "w", "c", "z", "x", "v", "g" };

	// 무시해야 하는 케이스
	public static final String[] IGNORE_CHARACTERS = { "`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=",
			"[", "]", "\\", ";", "\"", ".", "/", "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "{",
			"}", "|", ":", "\'", "<", ">", "?", " " };
	
	public static Set<String> IgnoreInfo = null;
	public static Map<String, Integer> ChosungInfo = null;
	public static Map<String, Integer> JungsungInfo = null;
	public static Map<String, Integer> JongsungInfo = null;
	

	public static void setInfo() {
		if (IgnoreInfo == null) {
			IgnoreInfo = getIgnoreInfo();
		}
		
		if (ChosungInfo == null) {
			ChosungInfo = getChosungInfo();
		}

		if (JungsungInfo == null) {
			JungsungInfo = getJungsungInfo();
		}

		if (JongsungInfo == null) {
			JongsungInfo = getJongsungInfo();
		}
	}

	public static Set<String> getIgnoreInfo() {
		Set<String> result = new HashSet<>();

		for (int i = 0; i < IGNORE_CHARACTERS.length; i++) {
			result.add(IGNORE_CHARACTERS[i]);
		}

		return result;
	}

	public static Map<String, Integer> getChosungInfo() {
		Map<String, Integer> result = new HashMap<String, Integer>();

		for (int i = 0; i < ENG_MAPPING_CHOSUNG.length; i++) {
			result.put(ENG_MAPPING_CHOSUNG[i], i);
		}

		return result;
	}

	public static Map<String, Integer> getJungsungInfo() {
		Map<String, Integer> result = new HashMap<String, Integer>();

		for (int i = 0; i < ENG_MAPPING_JUNGSUNG.length; i++) {
			result.put(ENG_MAPPING_JUNGSUNG[i], i);
		}

		return result;
	}

	public static Map<String, Integer> getJongsungInfo() {
		Map<String, Integer> result = new HashMap<String, Integer>();

		for (int i = 0; i < ENG_MAPPING_JONGSUNG.length; i++) {
			result.put(ENG_MAPPING_JONGSUNG[i], i);
		}

		return result;
	}

	public static Map<String, Integer> getChosungInfo(String str, int index) {
		Map<String, Integer> result = new HashMap<>();
		int code = 0;
		int idx = index;

		String findStr = str.substring(index, index + 1);
		if(ChosungInfo.containsKey(findStr)) {
			code = ChosungInfo.get(findStr) * 28 * 21;
			idx = index + 1;
		}

		result.put("code", code);
		result.put("index", idx);

		return result;
	}

	public static Map<String, Integer> getJungsungInfo(String str, int index) {
		Map<String, Integer> result = new HashMap<>();
		boolean flag = true;
		int code = -1;
		int idx = index;

		if (index + 2 <= str.length()) {
			// 2자리 먼저 찾기
			String findStrTwo = str.substring(index, index + 2);
			if(JungsungInfo.containsKey(findStrTwo)) {
				code = JungsungInfo.get(findStrTwo) * 28;
				idx = index + 2;
				flag = false;
			}
		}

		if (index + 1 <= str.length()) {
			if (flag) {
				// 1자리 찾기
				String findStr = str.substring(index, index + 1);
				
				if(JungsungInfo.containsKey(findStr)) {
					code = JungsungInfo.get(findStr) * 28;
					idx = index + 1;
				}
			}
		}

		result.put("code", code);
		result.put("index", idx);

		return result;
	}

	public static int findJongsung(String str, int index, int count) {
		int code = -1;

		if (index + count <= str.length()) {
			String findStr = str.substring(index, index + count);
			if(JongsungInfo.containsKey(findStr)) {
				code = JongsungInfo.get(findStr) + 1;
			}
		}

		return code;
	}

	public static Map<String, Integer> getJongsungInfo(String str, int index) {
		Map<String, Integer> result = new HashMap<>();
		Map<String, Integer> tmp = null;
		int code = 0;
		int idx = index;
		
		int codeTwo = findJongsung(str, index, 2);
		if (codeTwo != -1) {
			code = codeTwo;
			tmp = getJungsungInfo(str, index + 2);

			if (tmp.get("code") != -1) {
				code = findJongsung(str, index, 1);
			} else {
				idx = index + 1;
			}

		} else {
			tmp = getJungsungInfo(str, index + 1);
			if (tmp.get("code") != -1) {
				code = 0;
				idx = index - 1;
			} else {
				int codeOne = findJongsung(str, index, 1);
				if (codeOne == -1) {
					code = 0;
					idx = index - 1;
				} else {
					code = codeOne;
					idx = index;
				}
			}
		}

		result.put("code", code);
		result.put("index", idx);

		return result;
	}

	/**
	 * 유니코드 한글 구성 : code = 0xAC00 + ( 초성값 * 21 * 28 ) + ( 중성값 * 28 ) + ( 종성값 )
	 */
	public static String convertEng2Kor(String str) {

		setInfo();
		StringBuilder sb = new StringBuilder();
		
		int i = 0;
		while (i < str.length()) {
			if (IgnoreInfo.contains(str.substring(i, i + 1))) {
				sb.append(str.substring(i, i + 1));
				i++;
				continue;
			}

			Map<String, Integer> chosungInfo = getChosungInfo(str, i);
			int chosungCode = chosungInfo.get("code");
			i = chosungInfo.get("index");
			Map<String, Integer> jungsungInfo = getJungsungInfo(str, i);
			int jungsungCode = jungsungInfo.get("code");
			i = jungsungInfo.get("index");
			Map<String, Integer> jongsungInfo = getJongsungInfo(str, i);
			int jongsungCode = jongsungInfo.get("code");
			i = jongsungInfo.get("index");
			sb.append((char) (0xAC00 + chosungCode + jungsungCode + jongsungCode));
			i++;
		}

		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		System.out.println(convertEng2Kor("ahenrk rldjqrhk Wkrh thqlwkfmf thrdlfEo ghffh rldjqdmf rhdrurgks dbdlfgks dbxnqj!!!!"));
		System.out.println(convertEng2Kor("dkqjwl rkqkddp emfdjrktlsek."));
		System.out.println(convertEng2Kor("dkqjwlrk qkddp emfdjrktlsek."));
		System.out.println(convertEng2Kor("zhdRkrwl zhdRkrwl tkfkddml zhdRkrwl"));
	}

}
