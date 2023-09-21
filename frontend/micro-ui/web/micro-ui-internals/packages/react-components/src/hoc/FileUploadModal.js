import React,{useState,useMemo} from 'react'
import Modal from './Modal'
import { CloseSvg,UploadIcon,FileIcon,DeleteIconv2 } from '../atoms/svgindex';
import { FileUploader } from "react-drag-drop-files";

const FileUploadModal = ({heading,cancelLabel,submitLabel,onSubmit,onClose,t,fileTypes= [ "XLS", "XLSX"],multiple=true,fileValidator,onClickDownloadSample,...props}) => {

  const [file, setFile] = useState(null);

  const Heading = (props) => {
    return <h1 className="heading-m">{props.heading}</h1>;
  };

  const Close = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
      <path d="M0 0h24v24H0V0z" fill="#FFFFF" />
      <path fill="#0B0C0C" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
    </svg>
  );

  const CloseBtn = (props) => {
    return (
      <div onClick={props?.onClick} style={props?.isMobileView ? { padding: 5 } : null}>
        {props?.isMobileView ? (
          <CloseSvg />
        ) : (
          <div className={"icon-bg-secondary"} style={{backgroundColor:"#FFFFFF"}}>
            {" "}
            <Close />{" "}
          </div>
        )}
      </div>
    );
  };

  const dragDropJSX = <div className='drag-drop-container' >
    <UploadIcon />
    <p className='drag-drop-text'>{t("WBH_DRAG_DROP")} <text className='browse-text'>{t("WBH_BULK_BROWSE_FILES")}</text></p>
  </div>

  const renderFileCard = useMemo(() => {
    return (
      <div className='uploaded-file-container'>
        <div className='uploaded-file-container-sub'>
          <FileIcon className='icon' />
          <div style={{marginLeft:"0.5rem"}}>{file?.name}</div>
        </div>  
        <div className='icon' onClick={()=>setFile(null)}>
          <DeleteIconv2 />
        </div>
      </div>
    )
  }, [file])

  const handleChange = (file) => { 
    setFile(file)
  };

  return (
    <Modal
      headerBarMain={<Heading t={t} heading={t(heading)} />}
      headerBarEnd={<CloseBtn onClick={onClose} />}
      actionCancelLabel={t("WBH_DOWLOAD_TEMPLATE")}
      actionCancelOnSubmit={onClickDownloadSample}
      actionSaveLabel={t(submitLabel)}
      actionSaveOnSubmit={()=>onSubmit(file)}
      formId="modal-action"
      popupModuleActionBarStyles={{justifyContent:"space-between"}}
      isDisabled={file ? false : true}
    >
      <FileUploader handleChange={handleChange} name="file" types={fileTypes} children={dragDropJSX} onTypeError={fileValidator} />
      {file && renderFileCard}
    </Modal>
  )
}

export default FileUploadModal