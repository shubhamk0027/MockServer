package slackbot;

import com.slack.api.bolt.handler.builtin.BlockActionHandler;
import com.slack.api.bolt.handler.builtin.ViewSubmissionHandler;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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

public class AddTeam {

    private static final Logger logger = LoggerFactory.getLogger(AddTeam.class);

    // build view
    public static final View buildView() {
        return view(view -> view
                .callbackId("add-team")
                .type("modal")
                .notifyOnClose(true)
                .title(viewTitle(title -> title.type("plain_text").text("Create New Team").emoji(true)))
                .submit(viewSubmit(submit -> submit.type("plain_text").text("Submit").emoji(true)))
                .close(viewClose(close -> close.type("plain_text").text("Cancel").emoji(true)))
                .blocks(asBlocks(
                        input(input -> input
                                .blockId("name-block")
                                .element(plainTextInput(pti -> pti.actionId("add-team")))
                                .label(plainText(pt -> pt.text("Enter New Team Name").emoji(true)))
                        )
                ))
        );
    }

    // input validation--->
    public static final ViewSubmissionHandler submissionHandler = (req, ctx) -> {
        logger.info("Verifying team name");
        Map<String, Map <String, ViewState.Value>> stateValues = req.getPayload().getView().getState().getValues();
        String name = stateValues.get("name-block").get("add-team").getValue();
        Map<String, String> errors = new HashMap <>();
        for(int i=0;i<name.length();i++) {
            if(i==0 && !Character.isAlphabetic(name.charAt(i))){
                errors.put("name-block","Team name should start with an alphabet!");
                break;
            }else if(!Character.isLetterOrDigit(name.charAt(i))) {
                errors.put("name-block", "Team Name can not contain special characters!");
                break;
            }
        }
        if (!errors.isEmpty()) {
            return ctx.ack(r -> r.responseAction("errors").errors(errors));
        } else {
            logger.info("Full req->"+req.getPayload().getView().toString());
            logger.info("Finally submitted!");
            /**/ logger.info(stateValues.get("name-block").get("add-team").getValue());
            logger.info(req.getHeaders().toString());
            /**/ logger.info(req.getPayload().getUser().getUsername());
            return ctx.ack();
        }
    };

    // will be req in case of wron submission!
    public static final BlockActionHandler blockActionHandler = ((req, ctx) -> {
        logger.info("Values recieved: "+req.toString());
        logger.info(req.getHeaders().toString());
        logger.info(req.getPayload().getActions().get(0).getValue());
        return ctx.ack();
    });

}

