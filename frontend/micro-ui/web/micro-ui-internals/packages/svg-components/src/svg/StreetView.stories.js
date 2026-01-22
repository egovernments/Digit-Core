import React from "react";
import { StreetView } from "./StreetView";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "StreetView",
  component: StreetView,
};

export const Default = () => <StreetView />;
export const Fill = () => <StreetView fill="blue" />;
export const Size = () => <StreetView height="50" width="50" />;
export const CustomStyle = () => <StreetView style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <StreetView className="custom-class" />;

export const Clickable = () => <StreetView onClick={()=>console.log("clicked")} />;

const Template = (args) => <StreetView {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
