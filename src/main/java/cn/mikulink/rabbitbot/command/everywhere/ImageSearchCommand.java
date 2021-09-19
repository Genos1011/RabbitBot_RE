package cn.mikulink.rabbitbot.command.everywhere;

import cn.mikulink.rabbitbot.command.EverywhereCommand;
import cn.mikulink.rabbitbot.constant.ConstantImage;
import cn.mikulink.rabbitbot.constant.ConstantPixiv;
import cn.mikulink.rabbitbot.entity.CommandProperties;
import cn.mikulink.rabbitbot.entity.apirequest.saucenao.SaucenaoSearchInfoResult;
import cn.mikulink.rabbitbot.entity.pixiv.PixivImageInfo;
import cn.mikulink.rabbitbot.exceptions.RabbitException;
import cn.mikulink.rabbitbot.service.DanbooruService;
import cn.mikulink.rabbitbot.service.ImageService;
import cn.mikulink.rabbitbot.service.PixivService;
import cn.mikulink.rabbitbot.sys.annotate.Command;
import cn.mikulink.rabbitbot.utils.StringUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.internal.message.OnlineImage;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * @author MikuLink
 * @date 2020/02/19 16:10
 * for the Reisen
 * <p>
 * 搜图指令
 */
@Command
public class ImageSearchCommand extends BaseEveryWhereCommand {
    private static final Logger logger = LoggerFactory.getLogger(ImageSearchCommand.class);

    @Autowired
    private ImageService imageService;
    @Autowired
    private PixivService pixivService;
    @Autowired
    private DanbooruService danbooruService;

    @Override
    public CommandProperties properties() {
        return new CommandProperties("ImageSearch", "搜图");
    }

    @Override
    public Message execute(User sender, ArrayList<String> args, MessageChain messageChain, Contact subject) {
        if (null == args || args.size() == 0) {
            return new PlainText(ConstantImage.IMAGE_SEARCH_NO_IMAGE_INPUT);
        }
        //获取传入图片，解析mirai消息中的网络图片链接
        //[mirai:image:{FD4A1FC8-45F7-A3A9-FB8A-C75AFE71C47E}.mirai]
//         String temp_msg_part = messageChain.get(messageChain.size()-1).toString();
//        if(MiraiUtil.isMiraiImg(temp_msg_part)){
//        }
        //args中，图片被转为了字符串"[图片]"，这里直接判断args第一个参数是否为"[图片]",然后从messageChain中取索引为2的元素，忽略后面的图片
        if (!args.get(0).equals("[图片]")) {
            return new PlainText(ConstantImage.IMAGE_SEARCH_NO_IMAGE_INPUT);
        }

        //获取图片网络链接，gchat.qpic.cn是腾讯自家的
        //mirai中原图链接在messageChain中的图片元素下，但是需要强转
        //我看不太懂kotlin，先凑合着用吧,虽然可以运行，但代码报红，提示Usage of Kotlin internal declaration from different module
        //http://gchat.qpic.cn/gchatpic_new/455806936/3987173185-2655981981-FD4A1FC845F7A3A9FB8AC75AFE71C47E/0?term=2
        String imgUrl = ((OnlineImage) messageChain.get(2)).getOriginUrl();

        if (StringUtil.isEmpty(imgUrl)) {
            return new PlainText(ConstantImage.IMAGE_SEARCH_IMAGE_URL_PARSE_FAIL);
        }
        try {
            SaucenaoSearchInfoResult searchResult = imageService.searchImgFromSaucenao(imgUrl);
            if (null == searchResult) {
                //没有符合条件的图片，识图失败
                return new PlainText(ConstantImage.SAUCENAO_SEARCH_FAIL_PARAM);
            }

            //获取信息，并返回结果
            if (5 == searchResult.getHeader().getIndex_id()) {
                //pixiv
                PixivImageInfo pixivImageInfo = pixivService.getPixivImgInfoById((long) searchResult.getData().getPixiv_id());
                pixivImageInfo.setSender(sender);
                pixivImageInfo.setSubject(subject);
                return pixivService.parsePixivImgInfoByApiInfo(pixivImageInfo, searchResult.getHeader().getSimilarity());
            } else {
                //Danbooru
                return danbooruService.parseDanbooruImgRequest(searchResult);
            }
        } catch (RabbitException rabEx) {
            //业务异常，日志吃掉
            return new PlainText(rabEx.getMessage());
        } catch (FileNotFoundException fileNotFoundEx) {
            logger.warn(ConstantPixiv.PIXIV_IMAGE_DELETE + fileNotFoundEx.toString());
            return new PlainText(ConstantPixiv.PIXIV_IMAGE_DELETE);
        } catch (SocketTimeoutException timeoutException) {
            logger.error(ConstantImage.IMAGE_GET_TIMEOUT_ERROR + timeoutException.toString(), timeoutException);
            return new PlainText(ConstantImage.IMAGE_GET_TIMEOUT_ERROR);
        } catch (Exception ex) {
            logger.error(ConstantImage.IMAGE_GET_ERROR + ex.toString(), ex);
            return new PlainText(ConstantImage.IMAGE_GET_ERROR);
        }
    }
}
