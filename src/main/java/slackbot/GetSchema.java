package slackbot;

import com.slack.api.bolt.handler.builtin.BlockActionHandler;
import com.slack.api.bolt.handler.builtin.ViewSubmissionHandler;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.input;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.block.element.BlockElements.plainTextInput;
import static com.slack.api.model.view.Views.*;
import static com.slack.api.model.view.Views.view;
import static com.slack.api.model.view.Views.viewClose;
import static com.slack.api.model.view.Views.viewSubmit;
import static com.slack.api.model.view.Views.viewTitle;

public class GetSchema {

    private static final Logger logger = LoggerFactory.getLogger(GetSchema.class);

    // build view
    public static final View buildView() {
        return view(view -> view
                .callbackId("get-schema")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Enter Following Details").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .blocks(asBlocks(
                        input(input -> input
                                .blockId("teamKey-block")
                                .element(plainTextInput(pti -> pti.actionId("get-schema")))
                                .label(plainText(pt -> pt.text("Enter team name").emoji(true)))
                        ),
                        input(input -> input
                                .blockId("path-block")
                                .element(plainTextInput(pti -> pti.actionId("get-schema")))
                                .label(plainText(pt -> pt.text("Enter path *URI*").emoji(true)))
                        )
                ))
        );
    }

    // input validation--->
    public static final ViewSubmissionHandler submissionHandler = (req, ctx) -> {
        logger.info("Verifying get schema details");
        Map<String, Map <String, ViewState.Value>> stateValues = req.getPayload().getView().getState().getValues();
        String teamKey = stateValues.get("teamKey-block").get("get-schema").getValue();
        String path = stateValues.get("path-block").get("get-schema").getValue();
        // Send and Wait for acknolwedement
        // responsd to ctx
        logger.info("Received Schema Req "+teamKey+" for "+path);
        return ctx.ack();
    };

    public static final BlockActionHandler blockActionHandler = ((req, ctx) -> {
        return ctx.ack();
    });

}
