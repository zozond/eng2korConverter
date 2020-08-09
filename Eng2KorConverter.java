import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Eng2KorConverter {

	public static final String[] ENG_MAPPING_CHOSUNG = { "r", "R", "s", "e", "E", "f", "a", "q", "Q", "t", "T", "d",
			"w", "W", "c", "z", "x", "v", "g" };

	public static final String[] ENG_MAPPING_JUNGSUNG = { "k", "o", "i", "O", "j", "p", "u", "P", "h", "hk", "ho", "hl",
			"y", "n", "nj", "np", "nl", "b", "m", "ml", "l" };

	public static final String[] ENG_MAPPING_JONGSUNG = { "r", "R", "rt", "s", "sw", "sg", "e", "f", "fr", "fa", "fq",
			"ft", "fx", "fv", "fg", "a", "q", "qt", "t", "T", "d", "w", "c", "z", "x", "v", "g" };

	public static final String[] IGNORE_CHARACTERS = { "`", "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "=",
			"[", "]", "\\", ";", "\"", ".", "/", "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "{",
			"}", "|", ":", "\'", "<", ">", "?", " " };

	/**
		무시해야 하는 문자들의 정보를 Set으로 만들어서 리턴해 준다.
	*/
	public static Set<String> getIgnoreInfo() {
		Set<String> result = new HashSet<>();

		for (int i = 0; i < IGNORE_CHARACTERS.length; i++) {
			result.add(IGNORE_CHARACTERS[i]);
		}

		return result;
	}


	/**
		초성 정보를 받는다.
	 */
	public static Map<String, Integer> getChosungInfo(String str, int index) {
		Map<String, Integer> result = new HashMap<>();
		int code = 0;
		int idx = index;

		String findStr = str.substring(index, index + 1);
		for (int i = 0; i < ENG_MAPPING_CHOSUNG.length; i++) {
			if (ENG_MAPPING_CHOSUNG[i].equals(findStr)) {
				code = i * 28 * 21;
				idx = index + 1;
				break;
			}
		}

		result.put("code", code);
		result.put("index", idx);

		return result;
	}


	/**
		중성 정보를 받는다.
	 */
	public static Map<String, Integer> getJungsungInfo(String str, int index) {
		Map<String, Integer> result = new HashMap<>();
		boolean flag = true;
		int code = -1;
		int idx = index;

		if (index + 2 <= str.length()) {
			// 중성 정보가 2글자 일때...
			String findStrTwo = str.substring(index, index + 2);
			for (int i = 0; i < ENG_MAPPING_JUNGSUNG.length; i++) {
				if (ENG_MAPPING_JUNGSUNG[i].equals(findStrTwo)) {
					code = i * 28;
					idx = index + 2;
					flag = false;
					break;
				}
			}
		}

		if (index + 1 <= str.length()) {
			if (flag) {
				// 중성 정보가 한글자 일때...
				String findStr = str.substring(index, index + 1);
				for (int i = 0; i < ENG_MAPPING_JUNGSUNG.length; i++) {
					if (ENG_MAPPING_JUNGSUNG[i].equals(findStr)) {
						code = i * 28;
						idx = index + 1;
						break;
					}
				}
			}
		}

		result.put("code", code);
		result.put("index", idx);

		return result;
	}

	/* 종성이 두글자 일때 찾는것과 한글자 일때 찾는것을 이런식으로 하나의 함수로 만들었다. */
	public static int findJongsung(String str, int index, int count) {
		int code = -1;

		if (index + count <= str.length()) {
			String findStr = str.substring(index, index + count);
			for (int i = 0; i < ENG_MAPPING_JONGSUNG.length; i++) {
				if (ENG_MAPPING_JONGSUNG[i].equals(findStr)) {
					code = i + 1;
					break;
				}
			}
		}

		return code;
	}

	/**
		종성 정보를 받는다.
	 */
	public static Map<String, Integer> getJongsungInfo(String str, int index) {
		Map<String, Integer> result = new HashMap<>();
		int code = 0;
		int idx = index;

		int codeTwo = findJongsung(str, index, 2);
		if (codeTwo != -1) {
			code = codeTwo;
			Map<String, Integer> tmp = getJungsungInfo(str, index + 2);

			if (tmp.get("code") != -1) {
				code = findJongsung(str, index, 1);
			} else {
				idx = index + 1;
			}

		} else {
			Map<String, Integer> tmp = getJungsungInfo(str, index + 1);
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


	/* 컨버터 */
	public static String convertEng2Kor(String str) {

		StringBuilder sb = new StringBuilder();
		Set<String> ignoreCases = getIgnoreInfo();

		int i = 0;
		while (i < str.length()) {
			if (ignoreCases.contains(str.substring(i, i + 1))) {
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

			/* 유니코드 한글 구성 : code = 0xAC00 + ( 초성값 * 21 * 28 ) + ( 중성값 * 28 ) + ( 종성값 ) */
			sb.append((char) (0xAC00 + chosungCode + jungsungCode + jongsungCode));
			i++;
		}

		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		System.out.println(	convertEng2Kor("ahenrk rldjqrhk Wkrh thqlwkfmf thrdlfEo ghffh rldjqdmf rhdrurgks dbdlfgks dbxnqj!!!!"));
		System.out.println(	convertEng2Kor("dkqjwl rkqkddp emfdjrktlsek."));
		System.out.println(	convertEng2Kor("dkqjwlrk qkddp emfdjrktlsek."));
		System.out.println(	convertEng2Kor("zhdRkrwl zhdRkrwl tkfkddml zhdRkrwl"));
	}

}
