package com.example.goguma;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** OCR 텍스트의 키워드 점수로 카테고리(맛집, 카페, 쇼핑, 레시피, 기타)를 분류한다. */
@Component
public class CategoryClassifier {

    public static final String DEFAULT_CATEGORY = "기타";

    private static final Map<String, List<String>> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("카페", List.of("카페", "커피", "아메리카노", "라떼", "에스프레소", "디저트", "케이크",
                "베이커리", "빵", "스무디", "티라미수", "cafe", "coffee", "latte", "bakery"));
        KEYWORDS.put("맛집", List.of("맛집", "식당", "레스토랑", "메뉴", "음식", "고기", "삼겹살", "냉면", "국밥",
                "찌개", "파스타", "피자", "초밥", "라멘", "치킨", "웨이팅", "예약", "브런치", "술집", "포장",
                "restaurant", "menu"));
        KEYWORDS.put("레시피", List.of("레시피", "재료", "조리", "만드는 법", "만드는법", "끓", "볶", "썰", "큰술", "작은술",
                "계량", "분량", "다진", "양념", "반죽", "오븐", "인분", "tbsp", "tsp", "recipe", "ingredients"));
        KEYWORDS.put("쇼핑", List.of("쇼핑", "구매", "할인", "세일", "쿠폰", "배송", "장바구니", "가격", "무료배송",
                "브랜드", "의류", "신발", "가방", "택배", "주문", "원가", "sale", "shop", "price"));
    }

    /** 가장 점수가 높은 카테고리를 반환한다. 일치하는 키워드가 없으면 "기타". */
    public String classify(String text) {
        if (text == null || text.isBlank()) {
            return DEFAULT_CATEGORY;
        }
        String lower = text.toLowerCase();
        String best = DEFAULT_CATEGORY;
        int bestScore = 0;
        for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = entry.getKey();
            }
        }
        return best;
    }
}
