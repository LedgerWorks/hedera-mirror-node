package com.hedera.mirror.importer.parser.record;

import java.util.List;
import java.util.function.BiConsumer;
import javax.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;

import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.importer.exception.ImporterException;

@Log4j2
@Named
@Primary
@RequiredArgsConstructor
public class CompositeRecordItemListener implements RecordItemListener {

    private final List<RecordItemListener> recordItemListeners;

    private <T> void onEach(BiConsumer<RecordItemListener, T> consumer, T t) {
        for (RecordItemListener recordItemListener : recordItemListeners) {
            consumer.accept(recordItemListener, t);
        }
    }

    @Override
    public void onItem(RecordItem item) throws ImporterException {
        onEach(RecordItemListener::onItem, item);
    }
}
