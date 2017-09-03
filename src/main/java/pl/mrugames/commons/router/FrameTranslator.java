package pl.mrugames.commons.router;

import java.io.Serializable;

public interface FrameTranslator {
    Object translateToRequestOrResponse(Serializable serializable);

    Serializable translateFromRequest(Request request);
}
