package cn.iocoder.yudao.module.crm.convert.contact;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * crm联系人 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ContactConvert {

    ContactConvert INSTANCE = Mappers.getMapper(ContactConvert.class);

    ContactDO convert(ContactCreateReqVO bean);

    ContactDO convert(ContactUpdateReqVO bean);

    ContactRespVO convert(ContactDO bean);

    List<ContactRespVO> convertList(List<ContactDO> list);

    PageResult<ContactRespVO> convertPage(PageResult<ContactDO> page);

    List<ContactSimpleRespVO> convertAllList(List<ContactDO> list);

    @Mappings({
            @Mapping(target = "bizId", source = "reqVO.id"),
            @Mapping(target = "newOwnerUserId", source = "reqVO.id")
    })
    CrmPermissionTransferReqBO convert(CrmContactTransferReqVO reqVO, Long userId);

    /**
     * 转换详情信息
     *
     * @param contactDO 联系人
     * @param userMap 用户列表
     * @param crmCustomerDOList 客户
     * @return ContactRespVO
     */
    default  ContactRespVO convert(ContactDO contactDO, Map<Long, AdminUserRespDTO> userMap, List<CrmCustomerDO> crmCustomerDOList,
                                   List<ContactDO> contactList) {
        ContactRespVO contactVO = convert(contactDO);
        setUserInfo(contactVO, userMap);
        Map<Long,CrmCustomerDO> ustomerMap = crmCustomerDOList.stream().collect(Collectors.toMap(CrmCustomerDO::getId,v->v));
        Map<Long,ContactDO> contactMap = contactList.stream().collect(Collectors.toMap(ContactDO::getId,v->v));
        findAndThen(ustomerMap, contactDO.getCustomerId(), customer -> contactVO.setCustomerName(customer.getName()));
        findAndThen(contactMap, contactDO.getParentId(), contact -> contactVO.setParentName(contact.getName()));
        return contactVO;
    }
    default  List<ContactRespVO> converList(List<ContactDO> contactList, Map<Long, AdminUserRespDTO> userMap,
                                            List<CrmCustomerDO> customerList, List<ContactDO> parentContactList) {
        List<ContactRespVO> result = convertList(contactList);
        // TODO @zyna：简单的转换，可以使用 CollectionUtils.convertMap
        Map<Long, ContactDO> parentContactMap = parentContactList.stream().collect(Collectors.toMap(ContactDO::getId,v->v));
        Map<Long, CrmCustomerDO> customerMap = customerList.stream().collect(Collectors.toMap(CrmCustomerDO::getId,v->v));
        result.forEach(item -> {
            setUserInfo(item, userMap);
            findAndThen(customerMap,item.getCustomerId(),customer -> {item.setCustomerName(customer.getName());});
            findAndThen(parentContactMap,item.getParentId(),contactDO -> {item.setParentName(contactDO.getName());});
        });
        return result;
    }

    /**
     * 设置用户信息
     *
     * @param contactRespVO  联系人Response VO
     * @param userMap 用户信息 map
     */
    static void setUserInfo(ContactRespVO contactRespVO, Map<Long, AdminUserRespDTO> userMap){
        contactRespVO.setAreaName(AreaUtils.format(contactRespVO.getAreaId()));
        findAndThen(userMap, contactRespVO.getOwnerUserId(), user -> {
            contactRespVO.setOwnerUserName(user == null?"":user.getNickname());
        });
        findAndThen(userMap, Long.parseLong(contactRespVO.getCreator()), user -> contactRespVO.setCreatorName(user.getNickname()));
    }

}