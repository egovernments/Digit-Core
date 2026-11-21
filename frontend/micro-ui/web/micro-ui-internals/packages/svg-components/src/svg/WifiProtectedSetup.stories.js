import React from "react";
import { WifiProtectedSetup } from "./WifiProtectedSetup";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WifiProtectedSetup",
  component: WifiProtectedSetup,
};

export const Default = () => <WifiProtectedSetup />;
export const Fill = () => <WifiProtectedSetup fill="blue" />;
export const Size = () => <WifiProtectedSetup height="50" width="50" />;
export const CustomStyle = () => <WifiProtectedSetup style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WifiProtectedSetup className="custom-class" />;
export const Clickable = () => <WifiProtectedSetup onClick={()=>console.log("clicked")} />;

const Template = (args) => <WifiProtectedSetup {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
