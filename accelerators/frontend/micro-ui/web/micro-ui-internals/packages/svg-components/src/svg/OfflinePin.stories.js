import React from "react";
import { OfflinePin } from "./OfflinePin";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "OfflinePin",
  component: OfflinePin,
};

export const Default = () => <OfflinePin />;
export const Fill = () => <OfflinePin fill="blue" />;
export const Size = () => <OfflinePin height="50" width="50" />;
export const CustomStyle = () => <OfflinePin style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <OfflinePin className="custom-class" />;

export const Clickable = () => <OfflinePin onClick={()=>console.log("clicked")} />;

const Template = (args) => <OfflinePin {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
