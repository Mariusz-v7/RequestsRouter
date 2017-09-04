package pl.mrugames.commons.router;

import java.io.Serializable;

public interface FrameTranslator<T extends Serializable> {
    Object translateToRequestOrResponse(T serializable);

    T translateFromRequest(Request request);
}
