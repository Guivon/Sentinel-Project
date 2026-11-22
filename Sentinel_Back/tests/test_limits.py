from limits import WindowLimiter


def test_blocks_after_maximum_and_releases_after_window():
    lim = WindowLimiter(maximum=2, window_s=10)
    assert lim.try_acquire("a", now=0)[0] is True
    assert lim.try_acquire("a", now=1)[0] is True
    allowed, wait = lim.try_acquire("a", now=2)
    assert allowed is False and wait >= 1
    assert lim.try_acquire("a", now=11)[0] is True


def test_different_users_have_separate_counters():
    lim = WindowLimiter(maximum=1, window_s=10)
    assert lim.try_acquire("a", now=0)[0] is True
    assert lim.try_acquire("b", now=0)[0] is True
    assert lim.try_acquire("a", now=1)[0] is False
