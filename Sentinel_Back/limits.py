import threading
import time
from collections import defaultdict, deque


class WindowLimiter:
    def __init__(self, maximum: int, window_s: float) -> None:
        self.maximum = maximum
        self.window_s = window_s
        self._calls: dict[str, deque[float]] = defaultdict(deque)
        self._lock = threading.Lock()
        self._last_cleanup = time.monotonic()

    def try_acquire(self, key: str, now: float | None = None) -> tuple[bool, int]:

        now = time.monotonic() if now is None else now
        with self._lock:
            self._clean(now)
            queue = self._calls[key]
            while queue and now - queue[0] >= self.window_s:
                queue.popleft()
            if len(queue) >= self.maximum:
                wait = int(self.window_s - (now - queue[0])) + 1
                return False, max(wait, 1)
            queue.append(now)
            return True, 0

    def _clean(self, now: float) -> None:

        if now - self._last_cleanup < self.window_s:
            return
        self._last_cleanup = now
        for key in [k for k, queue in self._calls.items() if not queue or now - queue[-1] >= self.window_s]:
            del self._calls[key]
