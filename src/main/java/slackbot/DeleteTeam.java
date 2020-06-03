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

public class DeleteTeam {

    private static final Logger logger = LoggerFactory.getLogger(DeleteTeam.class);

    // build view
    public static final View buildView() {
        return view(view -> view
                .callbackId("del-team")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Enter API key").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .blocks(asBlocks(
                        input(input -> input
                                .blockId("apikey-block")
                                .element(plainTextInput(pti -> pti.actionId("del-team")))
                                .label(plainText(pt -> pt.text("Enter API Key of the team you want to delete").emoji(true)))
                        )
                ))
        );
    }

    // input validation--->
    public static final ViewSubmissionHandler submissionHandler = (req, ctx) -> {
        logger.info("Verifying api key");
        Map<String, Map <String, ViewState.Value>> stateValues = req.getPayload().getView().getState().getValues();
        String apikey = stateValues.get("apikey-block").get("del-team").getValue();
        String username = req.getPayload().getUser().getUsername();
        logger.info("Delete request from "+username +" with team key "+apikey);
        return ctx.ack();
    };

    public static final BlockActionHandler blockActionHandler = ((req, ctx) -> {
        return ctx.ack();
    });

}
