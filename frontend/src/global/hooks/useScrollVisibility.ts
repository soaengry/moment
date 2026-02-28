import { useState, useEffect, useCallback } from "react";

/**
 * 스크롤 방향에 따라 요소를 표시/숨김.
 * - 스크롤 위로 → visible (true)
 * - 스크롤 아래로 → hidden (false)
 * - 맨 위에 있으면 항상 visible
 */
export function useScrollVisibility() {
  const [isVisible, setIsVisible] = useState(true);
  const [lastScrollY, setLastScrollY] = useState(0);
  const [hasScroll, setHasScroll] = useState(false);

  const checkScrollable = useCallback(() => {
    const scrollable = document.documentElement.scrollHeight > window.innerHeight;
    setHasScroll(scrollable);
    if (!scrollable) {
      setIsVisible(true);
    }
  }, []);

  useEffect(() => {
    checkScrollable();

    const observer = new ResizeObserver(() => {
      checkScrollable();
    });
    observer.observe(document.body);

    return () => observer.disconnect();
  }, [checkScrollable]);

  useEffect(() => {
    if (!hasScroll) return;

    const handleScroll = () => {
      const currentScrollY = window.scrollY;

      if (currentScrollY <= 0) {
        setIsVisible(true);
      } else if (currentScrollY > lastScrollY) {
        setIsVisible(false);
      } else {
        setIsVisible(true);
      }

      setLastScrollY(currentScrollY);
    };

    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, [hasScroll, lastScrollY]);

  return isVisible;
}
