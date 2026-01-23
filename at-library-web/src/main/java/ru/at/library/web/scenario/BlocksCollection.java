package ru.at.library.web.scenario;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Обёртка над ElementsCollection для работы со списком блоков (CorePage)
 * как с "живым" списком, аналогично ElementsCollection.
 *
 * Каждый вызов snapshot()/iterator()/get() создаёт блоки по текущему состоянию DOM.
 */
public class BlocksCollection<T extends CorePage> implements List<T> {

    private final ElementsCollection roots;
    private final Class<? extends CorePage> blockClass;

    public BlocksCollection(ElementsCollection roots, Class<? extends CorePage> blockClass) {
        this.roots = roots;
        this.blockClass = blockClass;
    }

    public ElementsCollection getRoots() {
        return roots;
    }

    /**
     * Создаёт новый экземпляр блока для переданного корневого элемента.
     */
    @SuppressWarnings("unchecked")
    private T createBlock(SelenideElement root) {
        try {
            CorePage block = blockClass.getDeclaredConstructor().newInstance();
            block.setSelf(root);
            // Инициализируем элементы блока через Selenide, чтобы @Name-поля внутри него были доступны
            return (T) Selenide.page(block).initialize();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(String.format(
                    "Не удалось создать экземпляр блока %s",
                    blockClass.getName()), e);
        }
    }

    /**
     * Строит снимок текущего списка блоков по DOM.
     */
    private List<T> snapshot() {
        List<T> result = new ArrayList<>();
        for (SelenideElement root : roots) {
            result.add(createBlock(root));
        }
        return result;
    }

    // ---------- Основные методы чтения ----------

    @Override
    public int size() {
        return roots.size();
    }

    @Override
    public boolean isEmpty() {
        return roots.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return snapshot().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return snapshot().iterator();
    }

    @Override
    public Object[] toArray() {
        return snapshot().toArray();
    }

    @Override
    public <U> U[] toArray(U[] a) {
        return snapshot().toArray(a);
    }

    @Override
    public T get(int index) {
        return createBlock(roots.get(index));
    }

    @Override
    public int indexOf(Object o) {
        return snapshot().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return snapshot().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return snapshot().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return snapshot().listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return snapshot().subList(fromIndex, toIndex);
    }

    // ---------- Модифицирующие операции не поддерживаются ----------

    private static UnsupportedOperationException readOnly() {
        return new UnsupportedOperationException("BlocksCollection является только для чтения");
    }

    @Override
    public boolean add(T t) {
        throw readOnly();
    }

    @Override
    public boolean remove(Object o) {
        throw readOnly();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return snapshot().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw readOnly();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw readOnly();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw readOnly();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw readOnly();
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        throw readOnly();
    }

    @Override
    public void sort(Comparator<? super T> c) {
        throw readOnly();
    }

    @Override
    public void clear() {
        throw readOnly();
    }

    @Override
    public T set(int index, T element) {
        throw readOnly();
    }

    @Override
    public void add(int index, T element) {
        throw readOnly();
    }

    @Override
    public T remove(int index) {
        throw readOnly();
    }
}