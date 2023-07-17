import React from "react";
import { IosShare } from "./IosShare";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "IosShare",
  component: IosShare,
};

export const Default = () => <IosShare />;
export const Fill = () => <IosShare fill="blue" />;
export const Size = () => <IosShare height="50" width="50" />;
export const CustomStyle = () => <IosShare style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <IosShare className="custom-class" />;

export const Clickable = () => <IosShare onClick={()=>console.log("clicked")} />;

const Template = (args) => <IosShare {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
