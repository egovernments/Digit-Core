import React from "react";
import { OfflineShare } from "./OfflineShare";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "OfflineShare",
  component: OfflineShare,
};

export const Default = () => <OfflineShare />;
export const Fill = () => <OfflineShare fill="blue" />;
export const Size = () => <OfflineShare height="50" width="50" />;
export const CustomStyle = () => <OfflineShare style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OfflineShare className="custom-class" />;

export const Clickable = () => <OfflineShare onClick={()=>console.log("clicked")} />;

const Template = (args) => <OfflineShare {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
