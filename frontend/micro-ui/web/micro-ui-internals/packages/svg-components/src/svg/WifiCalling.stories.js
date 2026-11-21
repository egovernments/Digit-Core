import React from "react";
import { WifiCalling } from "./WifiCalling";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WifiCalling",
  component: WifiCalling,
};

export const Default = () => <WifiCalling />;
export const Fill = () => <WifiCalling fill="blue" />;
export const Size = () => <WifiCalling height="50" width="50" />;
export const CustomStyle = () => <WifiCalling style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WifiCalling className="custom-class" />;
export const Clickable = () => <WifiCalling onClick={()=>console.log("clicked")} />;

const Template = (args) => <WifiCalling {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
